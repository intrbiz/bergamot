package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.MatchOnAgentId;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.PassiveResultKey;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

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
        return super.accept(task) && AgentEngine.NAME.equals(task.getEngine()) && NAME.equals(task.getExecutor());
    }

    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent presence");
        try
        {
            // get the agent id
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // check the host presence
            ResultMO resultMO = new ActiveResultMO().fromCheck(executeCheck);
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                resultMO.ok("Bergamot Agent " + agent.getAgentName() + " connected");
            }
            else
            {
                resultMO.disconnected("Bergamot Agent disconnected");
            }
            // submit
            resultSubmitter.accept(resultMO);
        }
        catch (Exception e)
        {
            resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
    
    @Override
    public void start()
    {
        // setup event handlers
        // on connection
        this.getEngine().getAgentServer().setOnAgentRegisterHandler((handler) -> {
            // publish a passive result for the presence of this host
            if (logger.isTraceEnabled()) logger.trace("Got agent connection: " + handler.getAgentName() + " " + handler.getAgentId() + ", site: " + handler.getSiteId());
            // submit a passive result for the host
            this.publishResult(new PassiveResultKey(handler.getSiteId()), new PassiveResultMO().passive(handler.getSiteId(), new MatchOnAgentId(handler.getAgentId())).ok("Bergamot Agent " + handler.getAgentName() + " connected"));
        });
        // on disconnection
        this.getEngine().getAgentServer().setOnAgentUnregisterHandler((handler) -> {
            // publish a passive result for the presence of this host
            // debug log
            if (logger.isTraceEnabled()) logger.trace("Got agent disconnection: " + handler.getAgentName() + " " + handler.getAgentId() + ", site: " + handler.getSiteId());
            // submit a passive result for the host
            this.publishResult(new PassiveResultKey(handler.getSiteId()), new PassiveResultMO().passive(handler.getSiteId(), new MatchOnAgentId(handler.getAgentId())).disconnected("Bergamot Agent disconnected"));
        });
        // on ping?
        // disabled by default as this will send a result every 30 seconds
        if (Boolean.getBoolean("bergamot.agent.passive-result-on-ping"))
        {
            this.getEngine().getAgentServer().setOnAgentPingHandler((handler) -> {
                // publish a passive result for the presence of this host
                if (logger.isTraceEnabled()) logger.trace("Got agent ping: " + handler.getAgentName() + " " + handler.getAgentId() + ", site: " + handler.getSiteId());
                // submit a passive result for the host
                this.publishResult(new PassiveResultKey(handler.getSiteId()), new PassiveResultMO().passive(handler.getSiteId(), new MatchOnAgentId(handler.getAgentId())).ok("Bergamot Agent " + handler.getAgentName() + " connected"));
            });
        }
    }
}
