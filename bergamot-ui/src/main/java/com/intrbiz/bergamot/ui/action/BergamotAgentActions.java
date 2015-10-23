package com.intrbiz.bergamot.ui.action;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.SignAgentAccountingEvent;
import com.intrbiz.bergamot.crypto.util.CertificatePair;
import com.intrbiz.bergamot.crypto.util.CertificateRequest;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.crypto.util.RSAUtil;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignAgent;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotRootCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotSiteCA;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedAgent;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.metadata.Action;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.RoutingKey;

public class BergamotAgentActions
{
    private Logger logger = Logger.getLogger(BergamotAgentActions.class);
    
    private BergamotAgentManagerQueue queue;
    
    private RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client;
    
    private Accounting accounting = Accounting.create(BergamotAgentActions.class);
    
    public BergamotAgentActions()
    {
        this.queue = BergamotAgentManagerQueue.open();
        this.client = this.queue.createBergamotAgentManagerRPCClient();
    }
    
    @Action("sign-agent")
    public Certificate signAgent(UUID siteId, UUID agentId, CertificateRequest req, UUID contactId)
    {
        try
        {
            // sign the cert
            logger.info("Signing Bergamot Agent request for: " + req.getCommonName());
            AgentManagerResponse response = this.client.publish(new SignAgent(siteId, agentId, req.getCommonName(), PEMUtil.savePublicKey(req.getKey()))).get(10, TimeUnit.SECONDS);
            if (response instanceof SignedAgent)
            {
                Certificate crt = PEMUtil.loadCertificate(((SignedAgent) response).getCertificatePEM());                
                // account
                this.accounting.account(new SignAgentAccountingEvent(siteId, agentId, req.getCommonName(), ((X509Certificate) crt).getSerialNumber().toString(), contactId));
                // done
                return crt;
            }
            throw new RuntimeException("Failed to sign agent");
        }
        catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new RuntimeException("Failed to sign agent", e);
        }
    }
    
    @Action("sign-agent-key")
    public Certificate signAgentKey(UUID siteId, UUID agentId, String commonName, PublicKey key, UUID contactId)
    {
        try
        {
            // sign the cert
            logger.info("Signing Bergamot Agent request for: " + commonName);
            AgentManagerResponse response = this.client.publish(new SignAgent(siteId, agentId, commonName, PEMUtil.savePublicKey(key))).get(10, TimeUnit.SECONDS);
            if (response instanceof SignedAgent)
            {
                Certificate crt = PEMUtil.loadCertificate(((SignedAgent) response).getCertificatePEM());
                // account
                this.accounting.account(new SignAgentAccountingEvent(siteId, agentId, commonName, ((X509Certificate) crt).getSerialNumber().toString(), contactId));
                // done
                return crt;
            }
            throw new RuntimeException("Failed to sign agent key");
        }
        catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new RuntimeException("Failed to sign agent key", e);
        }
    }
    
    @Action("generate-agent")
    public CertificatePair generateAgent(UUID siteId, UUID agentId, String commonName, UUID contactId)
    {
        try
        {
            // generate the key pair
            KeyPair pair = RSAUtil.generateRSAKeyPair(2048);
            // sign the cert
            logger.info("Generating Bergamot Agent certificate pair: " + commonName);
            AgentManagerResponse response = this.client.publish(new SignAgent(siteId, agentId, commonName, PEMUtil.savePublicKey(pair.getPublic()))).get(10, TimeUnit.SECONDS);
            if (response instanceof SignedAgent)
            {
                CertificatePair crtPair = new CertificatePair((X509Certificate) PEMUtil.loadCertificate(((SignedAgent) response).getCertificatePEM()), pair.getPrivate());
                // account
                this.accounting.account(new SignAgentAccountingEvent(siteId, agentId, commonName, ((X509Certificate) crtPair.getCertificate()).getSerialNumber().toString(), contactId));
                // done
                return crtPair;
            }
            throw new RuntimeException("Failed to sign agent");
        }
        catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new RuntimeException("Failed to sign agent", e);
        }
    }
    
    @Action("get-site-ca")
    public Certificate getSiteCA(UUID siteId)
    {
        try
        {
            // get the site ca
            AgentManagerResponse response = this.client.publish(new GetSiteCA(siteId)).get(10, TimeUnit.SECONDS);
            if (response instanceof GotSiteCA)
            {
                return PEMUtil.loadCertificate(((GotSiteCA) response).getCertificatePEM());
            }
            throw new RuntimeException("Failed to get site CA");
        }
        catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new RuntimeException("Failed to get site CA", e);
        }
    }
    
    @Action("get-root-ca")
    public Certificate getRootCA()
    {
        try
        {
            // get the site ca
            AgentManagerResponse response = this.client.publish(new GetRootCA()).get(10, TimeUnit.SECONDS);
            if (response instanceof GotRootCA)
            {
                return PEMUtil.loadCertificate(((GotRootCA) response).getCertificatePEM());
            }
            throw new RuntimeException("Failed to get root CA");
        }
        catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new RuntimeException("Failed to get root CA", e);
        }
    }
}
