package com.intrbiz.bergamot.worker.engine.agent;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.server.BergamotAgentServer;
import com.intrbiz.bergamot.model.message.processor.agent.AgentRegister;
import com.intrbiz.bergamot.worker.engine.AbstractEngine;
import com.intrbiz.bergamot.worker.engine.EngineContext;

/**
 * Execute Bergamot Agent checks via the Bergamot Agent Server
 */
public class AgentEngine extends AbstractEngine
{
    private static final Logger logger = Logger.getLogger(AgentEngine.class);
    
    public static final String NAME = "agent";
    
    private BergamotAgentServer server;

    public AgentEngine()
    {
        super(NAME,
                new PresenceExecutor(), 
                new CPUExecutor(), 
                new MemoryExecutor(), 
                new DiskExecutor(),
                new DisksExecutor(),
                new OSExecutor(),
                new UptimeExecutor(),
                new NagiosExecutor(),
                new UsersExecutor(),
                new ProcessesExecutor(),
                new ProcessStatsExecutor(),
                new AgentExecutor(),
                new AgentMemoryExecutor(),
                new NetConExecutor(),
                new PortListenerExecutor(),
                new NetIOExecutor(),
                new DiskIOExecutor(),
                new LoadExecutor(),
                new MetricsExecutor(),
                new ScriptExecutor()
        );
    }
    
    @Override
    public void doPrepare(EngineContext engineContext) throws Exception
    {
        // setup the server
        int port = engineContext.getIntParameter("agent-port", 15080);
        logger.info("Accepting Bergamot Agent connections on port " + port);
        this.server = new BergamotAgentServer(port, engineContext::lookupAgentKey);
    }
    
    @Override
    public void doStart(EngineContext engineContext) throws Exception
    {
        // Setup event handlers
        this.server.setOnAgentConnectHandler((agent) -> {
            logger.info("Registering agent: " + agent.getAgentId());
            engineContext.registerAgent(agent.getAgentId());
            // Offer a register message
            if (! Util.isEmpty(agent.getAgentTemplateName()))
            {
                logger.info("Sending agent register message");
                engineContext.publishAgentAction(new AgentRegister(
                    agent.getSiteId(), 
                    agent.getAgentId(), 
                    agent.getAgentKeyId(), 
                    agent.getAgentHostName(), 
                    agent.getAgentHostSummary(), 
                    agent.getAgentAddress(), 
                    agent.getAgentTemplateName()
                ));
            }
        });
        this.server.setOnAgentDisconnectHandler((agent) -> {
            logger.debug("Unregistering agent: " + agent.getAgentId());
            engineContext.unregisterAgent(agent.getAgentId());
        });
        // start the server
        this.server.start();
    }

    public BergamotAgentServer getAgentServer()
    {
        return this.server;
    }
}
