package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.MatchOnAgentId;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.bergamot.worker.engine.EngineContext;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;

/**
 * Check the presence of a Bergamot Agent
 */
public class PresenceExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "presence";
    
    private Logger logger = Logger.getLogger(PresenceExecutor.class);

    public PresenceExecutor()
    {
        super();
    }

    /**
     * Only execute Checks where the engine == "agent"
     */
    @Override
    public boolean accept(ExecuteCheck task)
    {
        return AgentEngine.NAME.equals(task.getEngine()) && NAME.equals(task.getExecutor());
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent presence");
        try
        {
            // get the agent id
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                agent.sendOnePingAndOnePingOnly((rtt) -> {
                    context.publishActiveResult(new ActiveResultMO().fromCheck(executeCheck).ok("Bergamot Agent " + agent.getAgentUserAgent() + " connected. Latency: " + rtt + "ms").runtime(rtt));
                    context.publishReading(executeCheck, new LongGaugeReading("latency", "ms", rtt));
                });
            }
            else
            {
                context.publishActiveResult(new ActiveResultMO().fromCheck(executeCheck).disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            context.publishActiveResult(new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
    
    @Override
    public void start(AgentEngine engine, EngineContext context)
    {
        // setup event handlers
        // on connection
        this.getEngine().getAgentServer().setOnAgentConnectHandler((handler) -> {
            // publish a passive result for the presence of this host
            if (logger.isTraceEnabled()) logger.trace("Got agent connection: " + handler.getAgentUserAgent() + " " + handler.getAgentId() + ", site: " + handler.getSiteId());
            // submit a passive result for the host
            context.publishResult(new PassiveResultMO().passive(handler.getSiteId(), new MatchOnAgentId(handler.getAgentId())).ok("Bergamot Agent " + handler.getAgentUserAgent() + " connected"));
        });
        // on disconnection
        this.getEngine().getAgentServer().setOnAgentDisconnectHandler((handler) -> {
            // publish a passive result for the presence of this host
            // debug log
            if (logger.isTraceEnabled()) logger.trace("Got agent disconnection: " + handler.getAgentUserAgent() + " " + handler.getAgentId() + ", site: " + handler.getSiteId());
            // submit a passive result for the host
            context.publishResult(new PassiveResultMO().passive(handler.getSiteId(), new MatchOnAgentId(handler.getAgentId())).disconnected("Bergamot Agent disconnected"));
        });
        // on ping?
        // disabled by default as this will send a result every 30 seconds
        if (Boolean.getBoolean("bergamot.agent.passive-result-on-ping"))
        {
            this.getEngine().getAgentServer().setOnAgentPingHandler((handler) -> {
                // publish a passive result for the presence of this host
                if (logger.isTraceEnabled()) logger.trace("Got agent ping: " + handler.getAgentUserAgent() + " " + handler.getAgentId() + ", site: " + handler.getSiteId());
                // submit a passive result for the host
                context.publishResult(new PassiveResultMO().passive(handler.getSiteId(), new MatchOnAgentId(handler.getAgentId())).ok("Bergamot Agent " + handler.getAgentUserAgent() + " connected"));
            });
        }
    }
}
