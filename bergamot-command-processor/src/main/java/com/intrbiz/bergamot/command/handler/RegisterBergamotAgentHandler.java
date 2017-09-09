package com.intrbiz.bergamot.command.handler;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.SignAgentAccountingEvent;
import com.intrbiz.bergamot.command.CommandProcessor;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.crypto.util.SerialNum;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.importer.BergamotConfigImporter;
import com.intrbiz.bergamot.importer.BergamotImportReport;
import com.intrbiz.bergamot.model.AgentRegistration;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.SignAgent;
import com.intrbiz.bergamot.model.message.agent.manager.response.SignedAgent;
import com.intrbiz.bergamot.model.message.command.CommandResponse;
import com.intrbiz.bergamot.model.message.command.GeneralCommandError;
import com.intrbiz.bergamot.model.message.command.RegisterBergamotAgent;
import com.intrbiz.bergamot.model.message.command.RegisteredBergamotAgent;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.RoutingKey;

public class RegisterBergamotAgentHandler implements BergamotCommandHandler<RegisterBergamotAgent>
{
    private Logger logger = Logger.getLogger(RegisterBergamotAgentHandler.class);
    
    private BergamotAgentManagerQueue queue;
    
    private RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client;
    
    private Accounting accounting = Accounting.create(RegisterBergamotAgentHandler.class);
    
    public void init(CommandProcessor processor)
    {
        this.queue = BergamotAgentManagerQueue.open();
        this.client = this.queue.createBergamotAgentManagerRPCClient();
    }
    
    @Override
    public CommandResponse process(RegisterBergamotAgent request)
    {
        try
        {
            logger.info("Registering agent: " + request.getCommonName() + " (" + request.getAgentId() + ")");
            // lookup the details provided
            try (BergamotDB db = BergamotDB.connect())
            {
                // lookup the template
                Site site = db.getSite(Site.getSiteId(request.getAgentId()));
                if (site == null) throw new RuntimeException("No such site");
                Config template = db.getConfig(request.getTemplateId());
                if (template == null) throw new RuntimeException("No such template");
                HostCfg hostTemplate = (HostCfg) template.getConfiguration();
                // assert that the template is a Host template
                if (! (template.getConfiguration() instanceof HostCfg)) throw new RuntimeException("The template must be a host template");
                // call to the agent manager to sign the certificate
                AgentManagerResponse response = this.client.publish(new SignAgent(site.getId(), request.getAgentId(), request.getCommonName(), request.getPublicKey())).get(10, TimeUnit.SECONDS);
                if (response instanceof SignedAgent)
                {
                    Certificate crt = PEMUtil.loadCertificate(((SignedAgent) response).getCertificatePEM());
                    logger.info("Successfully signed agent certificate: " + crt);
                    // account
                    this.accounting.account(new SignAgentAccountingEvent(site.getId(), request.getAgentId(), request.getCommonName(), ((X509Certificate) crt).getSerialNumber().toString(), null));
                    // store the certificate
                    db.setAgentRegistration(new AgentRegistration(site.getId(), request.getAgentId(), request.getCommonName(), SerialNum.fromBigInt(((X509Certificate) crt).getSerialNumber()).toString()));
                    // create a host based on the template
                    this.applyConfigurationChange(db, request, site, hostTemplate);
                    // got a certificate and registered the host
                    RegisteredBergamotAgent resp = new RegisteredBergamotAgent();
                    resp.setAgentId(request.getAgentId());
                    resp.setCertificate(((SignedAgent) response).getCertificatePEM());
                    resp.setCommonName(request.getCommonName());
                    return resp;
                }
                else
                {
                    return new GeneralCommandError("Failed to sign agent certificate");
                }
            }
        }
        catch (Exception e)
        {
            return new GeneralCommandError(e.getMessage());
        }
    }
    
    private void applyConfigurationChange(BergamotDB db, RegisterBergamotAgent request, Site site, HostCfg template)
    {
        // create the configuration change
        BergamotCfg changeCfg = new BergamotCfg();
        changeCfg.setSite(site.getName());
        changeCfg.setSummary("Auto register host: " + request.getCommonName());
        // the host we are adding
        HostCfg host = new HostCfg();
        host.setAgentId(request.getAgentId());
        host.setName(request.getCommonName());
        if (! Util.isEmpty(template.getSummary()))
        {
            host.setSummary(request.getCommonName() + " (" + template.getSummary() + ")");
        }
        else
        {
            host.setSummary(request.getCommonName());
        }
        host.getInheritedTemplates().add(template.getName());
        changeCfg.addObject(host);
        // log the config change
        ConfigChange change = new ConfigChange(site.getId(), null, changeCfg);
        db.setConfigChange(change);
        // apply it
        ValidatedBergamotConfiguration validatedConfiguration = changeCfg.validate(db.getObjectLocator(site.getId()));
        if (validatedConfiguration.getReport().isValid())
        {
            // good we have a valid configuration, import it
            BergamotConfigImporter importer = new BergamotConfigImporter(validatedConfiguration).online(true);
            BergamotImportReport importReport = importer.importConfiguration();
            if (importReport.isSuccessful())
            {
                // whoop, we successfully registered the host
                change.setApplied(true);
                change.setAppliedAt(new Timestamp(System.currentTimeMillis()));
                db.setConfigChange(change);
            }
            else
            {
                logger.error("Failed to auto register host, the configuration import failed");
                // keep the config change, it can be manually applied later
                // TODO: raise a notifcation in this instance    
            }
        }
        else
        {
            logger.error("Failed to auto register host, the resultant configuration is not valid");
            // keep the config change, it can be manually applied later
            // TODO: raise a notifcation in this instance
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends RegisterBergamotAgent>[] handles()
    {
        return new Class[] { RegisterBergamotAgent.class };
    }
}
