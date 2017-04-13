package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServer;
import com.intrbiz.bergamot.agent.server.BergamotAgentServer.RegisterAgentCallback.SendAgentRegistrationMessage;
import com.intrbiz.bergamot.agent.server.config.BergamotAgentServerCfg;
import com.intrbiz.bergamot.model.message.SiteMO;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.model.message.agent.manager.request.GetServer;
import com.intrbiz.bergamot.model.message.agent.manager.response.AgentManagerError;
import com.intrbiz.bergamot.model.message.agent.manager.response.GotServer;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationComplete;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationFailed;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationFailed.ErrorCode;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationRequest;
import com.intrbiz.bergamot.model.message.command.GeneralCommandError;
import com.intrbiz.bergamot.model.message.command.RegisterBergamotAgent;
import com.intrbiz.bergamot.model.message.command.RegisteredBergamotAgent;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
import com.intrbiz.bergamot.worker.config.AgentWorkerCfg;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.name.RoutingKey;

/**
 * Execute Bergamot Agent checks via the Bergamot Agent Server
 */
public class AgentEngine extends AbstractEngine
{
    private static final Logger logger = Logger.getLogger(AgentEngine.class);
    
    public static final String NAME = "agent";
    
    private BergamotAgentServer agentServer;

    public AgentEngine()
    {
        super(NAME);
    }
    
    @Override
    public boolean isAgentRouted()
    {
        return true;
    }

    @Override
    protected void configure() throws Exception
    {
        super.configure();
        // setup executors
        if (this.executors.isEmpty())
        {
            this.addExecutor(new PresenceExecutor());
            this.addExecutor(new CPUExecutor());
            this.addExecutor(new MemoryExecutor());
            this.addExecutor(new DiskExecutor());
            this.addExecutor(new DisksExecutor());
            this.addExecutor(new OSExecutor());
            this.addExecutor(new UptimeExecutor());
            this.addExecutor(new NagiosExecutor());
            this.addExecutor(new UsersExecutor());
            this.addExecutor(new ProcessesExecutor());
            this.addExecutor(new ProcessStatsExecutor());
            this.addExecutor(new AgentExecutor());
            this.addExecutor(new AgentMemoryExecutor());
            this.addExecutor(new NetConExecutor());
            this.addExecutor(new PortListenerExecutor());
            this.addExecutor(new NetIOExecutor());
            this.addExecutor(new DiskIOExecutor());
            this.addExecutor(new LoadExecutor());
        }
    }
    
    @Override
    public void start() throws Exception
    {
        // create our agent server
        this.agentServer = new BergamotAgentServer();
        AgentWorkerCfg workerCfg = (AgentWorkerCfg) this.getWorker().getConfiguration();
        BergamotAgentServerCfg serverCfg = workerCfg.getAgentServer();
        // use a static configuration or dynamically request certificates from the agent manager
        if (serverCfg == null)
        {
            logger.info("Requesting agent server certificate from agent manager, for: " + workerCfg.getName());
            serverCfg = requestAgentServerCertificates(workerCfg.getName(), workerCfg.getPort()); 
        }
        // configure the agent server with a static configuration            
        logger.info("Listening for Bergamot Agent connections on port: " + serverCfg.getPort());
        this.agentServer.configure(serverCfg);
        // handle binding / unbinding agent routes when an agent connects and disconnects
        this.agentServer.setOnAgentRegisterHandler((handler) -> {
            this.bindAgent(handler.getAgentId());
        });
        this.agentServer.setOnAgentUnregisterHandler((handler) -> {
            // unbind the agent from this worker so that active checks will not be routed to us
            this.unbindAgent(handler.getAgentId());
        });
        // setup the registration callback
        this.agentServer.setOnRequestAgentRegistration(this::registerAgent);
        // setup queues etc
        super.start();
        // start the agent server
        this.agentServer.start();
    }
    
    protected BergamotAgentServerCfg requestAgentServerCertificates(String name, int port) throws Exception
    {
        try (BergamotAgentManagerQueue agentManager = BergamotAgentManagerQueue.open())
        {
            try (RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client = agentManager.createBergamotAgentManagerRPCClient())
            {
                BergamotAgentServerCfg cfg = new BergamotAgentServerCfg();
                cfg.setPort(port);
                cfg.setName(name);
                // get the root CA
                GotServer server = this.callAgentManager(client, new GetServer(name).withRoot().withKey().withGenerate(), 60);
                // complete the config
                cfg.setCaCertificate(server.getRootCertificatePEM());
                cfg.setCertificate(server.getCertificatePEM());
                cfg.setKey(server.getKeyPEM());
                return cfg;
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to request agent server certificates from the agent manager", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends AgentManagerResponse> T callAgentManager(RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> client, AgentManagerRequest request, int timeoutSeconds) throws Exception
    {
        AgentManagerResponse response = client.publish(request).get(timeoutSeconds, TimeUnit.SECONDS);
        if (response instanceof AgentManagerError)
        {
            AgentManagerError error = (AgentManagerError) response;
            throw new RuntimeException("Failed to call agent manager: " + error.getMessage());
        }
        return (T) response;
    }

    public BergamotAgentServer getAgentServer()
    {
        return this.agentServer;
    }
    
    public void registerAgent(UUID templateId, final AgentRegistrationRequest request, final SendAgentRegistrationMessage callback)
    {
        // mask the agentId with the siteId from the template
        UUID agentId = SiteMO.setSiteId(SiteMO.getSiteId(templateId), request.getAgentId());
        // build the command
        RegisterBergamotAgent command = new RegisterBergamotAgent();
        command.setAgentId(agentId);
        command.setCommonName(request.getCommonName());
        command.setTemplateId(templateId);
        command.setPublicKey(request.getPublicKey());
        // log
        logger.info("Got request to register agent using template: " + templateId + ", agent id: " + request.getAgentId() + " common name: " + request.getCommonName());
        // send the command
        this.getCommandRPCClient().publish(60_000L, command, (response) -> {
            if (response instanceof RegisteredBergamotAgent)
            {
                // all good
                RegisteredBergamotAgent rba = (RegisteredBergamotAgent) response;
                callback.send(new AgentRegistrationComplete(request, rba.getAgentId(), rba.getCommonName(), rba.getCertificate()));
            }
            else if (response instanceof GeneralCommandError)
            {
                callback.send(new AgentRegistrationFailed(request, ErrorCode.GENERAL, ((GeneralCommandError) response).getMessage()));
            }
            else
            {
                callback.send(new AgentRegistrationFailed(request, ErrorCode.GENERAL, "Unknown"));    
            }
        }, (error) -> {
            callback.send(new AgentRegistrationFailed(request, ErrorCode.GENERAL, error.getMessage()));
        });
    }
}
