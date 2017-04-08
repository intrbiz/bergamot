package com.intrbiz.bergamot.agent.manager;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.Certificate;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.manager.config.BergamotAgentManagerCfg;
import com.intrbiz.bergamot.agent.manager.signer.CertificateManager;
import com.intrbiz.bergamot.agent.manager.store.BergamotKeyStore;
import com.intrbiz.bergamot.crypto.util.CertificatePair;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.health.HealthAgent;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.CreateSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetAgent;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetServer;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignAgent;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignServer;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignTemplate;
import com.intrbiz.bergamot.model.message.agent.manager.response.AgentManagerError;
import com.intrbiz.bergamot.model.message.agent.manager.response.CreatedSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotAgent;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotServer;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedAgent;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedServer;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedTemplate;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RPCHandler;
import com.intrbiz.queue.RPCServer;
import com.intrbiz.queue.rabbit.RabbitPool;

/**
 * The Bergamot Agent Manager is a dedicated services which managed the handling of TLS certificates and keys for Bergamot Agents.
 * 
 * This allows the Bergamot Agent Manager to be run on a dedicated, highly secured machine which only has outbound access to RabbitMQ, this isolation is designed to keep the CA private keys as safe as possible without requiring the use of a HSM.
 * 
 */
public class BergamotAgentManager implements Configurable<BergamotAgentManagerCfg>, RPCHandler<AgentManagerRequest, AgentManagerResponse>
{
    private Logger logger = Logger.getLogger(BergamotAgentManager.class);
    
    private BergamotAgentManagerCfg cfg;
    
    private BergamotKeyStore keyStore;

    private CertificateManager certificateManager;
    
    private BergamotAgentManagerQueue queue;
    
    private RPCServer<AgentManagerRequest, AgentManagerResponse> server;

    public BergamotAgentManager()
    {
        super();
    }

    @Override
    public void configure(BergamotAgentManagerCfg cfg) throws Exception
    {
        this.cfg = cfg;
        // setup the key store
        this.keyStore = (BergamotKeyStore) cfg.getKeyStore().create();
        // setup the certificate manager
        this.certificateManager = new CertificateManager(this.keyStore, this.cfg.getCertName());
    }

    @Override
    public BergamotAgentManagerCfg getConfiguration()
    {
        return this.cfg;
    }
    
    public void start()
    {
        // ensure we have a root cert
        this.certificateManager.generateRootCA();
        // verify the keystore
        this.keyStore.check();
        // setup the RPC server
        this.queue  = BergamotAgentManagerQueue.open();
        this.server = this.queue.createBergamotAgentManagerRPCServer(this);
        // start the health agent
        HealthAgent.getInstance().init("agent-manager", "bergamot-agent-manager");
        // whoo all up
        logger.info("Bergamot Agent Manager started!");
    }

    @Override
    public AgentManagerResponse handleDevliery(AgentManagerRequest event) throws IOException
    {
        try
        {
            if (event instanceof GetRootCA)
            {
                return new GotRootCA(this.keyStore.loadRootCA().getCertificateAsPEM());
            }
            else if (event instanceof GetSiteCA)
            {
                return new GotSiteCA(this.keyStore.loadSiteCA(((GetSiteCA) event).getSiteId()).getCertificateAsPEM());
            }
            else if (event instanceof GetAgent)
            {
                GetAgent agent = (GetAgent) event;
                return new GotAgent(this.keyStore.loadAgent(agent.getSiteId(), agent.getId()).getCertificateAsPEM());
            }
            else if (event instanceof GetServer)
            {
                GetServer server = (GetServer) event;
                // do we have the server
                if (this.keyStore.hasServer(server.getCommonName()))
                {
                    // load the server key
                    CertificatePair serverPair = this.keyStore.loadServer(server.getCommonName());
                    GotServer resp = new GotServer(serverPair.getCertificateAsPEM());
                    // do we want the key?
                    if (server.isIncludeKey() && serverPair.getKey() != null)
                        resp.setKeyPEM(serverPair.getKeyAsPEM());
                    // do we want the CA cert
                    if (server.isIncludeRoot())
                        resp.setRootCertificatePEM(this.keyStore.loadRootCA().getCertificateAsPEM());
                    return resp;
                }
                else if (server.isGenerate())
                {
                    // generate the certificate
                    CertificatePair serverPair = this.certificateManager.signServer(server.getCommonName(), null);
                    GotServer resp = new GotServer();
                    resp.setCertificatePEM(serverPair.getCertificateAsPEM());
                    // do we want the key?
                    if (server.isIncludeKey() && serverPair.getKey() != null)
                        resp.setKeyPEM(serverPair.getKeyAsPEM());
                    // do we want the CA cert
                    if (server.isIncludeRoot())
                        resp.setRootCertificatePEM(this.keyStore.loadRootCA().getCertificateAsPEM());
                    return resp;
                }
                else
                {
                    throw new RuntimeException("Failed to load certificate for server: " + server.getCommonName());
                }
            }
            else if (event instanceof CreateSiteCA)
            {
                CreateSiteCA createSite = (CreateSiteCA) event;
                if (createSite.getSiteId() == null || Util.isEmpty(createSite.getSiteName())) return new AgentManagerError("Invalid request");
                // create a site CA
                Certificate cert = this.certificateManager.generateSiteCA(createSite.getSiteId(), createSite.getSiteName());
                // respond
                return new CreatedSiteCA(PEMUtil.saveCertificate(cert));
            }
            else if (event instanceof SignAgent)
            {
                SignAgent sign = (SignAgent) event;
                // sign the agent 
                Certificate cert = this.certificateManager.signAgent(sign.getSiteId(), sign.getId(), sign.getCommonName(), PEMUtil.loadPublicKey(sign.getPublicKeyPEM()));
                // respond
                return new SignedAgent(PEMUtil.saveCertificate(cert));
            }
            else if (event instanceof SignServer)
            {
                SignServer sign = (SignServer) event;
                // sign the server 
                CertificatePair cert = this.certificateManager.signServer(sign.getCommonName(), PEMUtil.loadPublicKey(sign.getPublicKeyPEM()));
                // respond
                return new SignedServer(cert.getCertificateAsPEM());
            }
            else if (event instanceof SignTemplate)
            {
                SignTemplate sign = (SignTemplate) event;
                // sign the agent 
                Certificate cert = this.certificateManager.signTemplate(sign.getSiteId(), sign.getId(), sign.getTemplateName(), PEMUtil.loadPublicKey(sign.getPublicKeyPEM()));
                // respond
                return new SignedTemplate(PEMUtil.saveCertificate(cert));
            }
        }
        catch (Exception e)
        {
            this.logger.error("Error processing manager request", e);
            // we don't provide error messages to the client for security reasons!
            return new AgentManagerError("Request failed");
        }
        return new AgentManagerError("Request not supported");
    }
    
    public void shutdown()
    {
        this.server.close();
        this.queue.close();
    }

    public static void main(String[] args) throws Exception
    {
        // setup logging
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        // load the config
        File configFile = new File(Util.coalesceEmpty(System.getProperty("bergamot.config"), System.getenv("bergamot_config"), System.getenv("BERGAMOT_CONFIG"), "/etc/bergamot/agent-manager.xml"));
        Logger.getLogger(BergamotAgentManager.class).info("Reading configuration file " + configFile.getAbsolutePath());
        BergamotAgentManagerCfg config = Configuration.read(BergamotAgentManagerCfg.class, new FileInputStream(configFile));
        config.applyDefaults();
        // setup the queue broker
        QueueManager.getInstance().registerDefaultBroker(new RabbitPool(config.getBroker().getUrl(), config.getBroker().getUsername(), config.getBroker().getPassword()));
        // create the manager
        BergamotAgentManager manager = new BergamotAgentManager();
        manager.configure(config);
        // start
        manager.start();
    }
}
