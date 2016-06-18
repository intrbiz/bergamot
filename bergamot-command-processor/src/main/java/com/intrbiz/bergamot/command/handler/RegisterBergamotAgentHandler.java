package com.intrbiz.bergamot.command.handler;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.accounting.Accounting;
import com.intrbiz.bergamot.accounting.model.SignAgentAccountingEvent;
import com.intrbiz.bergamot.command.CommandProcessor;
import com.intrbiz.bergamot.crypto.util.PEMUtil;
import com.intrbiz.bergamot.crypto.util.SerialNum;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.AgentRegistration;
import com.intrbiz.bergamot.model.Config;
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
                Config template = db.getConfig(request.getAgentId());
                if (template == null) throw new RuntimeException("No such template");
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
                    // response
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

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends RegisterBergamotAgent>[] handles()
    {
        return new Class[] { RegisterBergamotAgent.class };
    }
}
