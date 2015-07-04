package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.stat.AgentStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.util.UnitUtil;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

/**
 * Check internal memory usage of a Bergamot Agent
 */
public class AgentMemoryExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "agent-memory";
    
    private Logger logger = Logger.getLogger(AgentMemoryExecutor.class);

    public AgentMemoryExecutor()
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
    public void execute(ExecuteCheck executeCheck)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent memory");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // get the Agent stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(new CheckAgent(), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    AgentStat stat = (AgentStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got agent info in " + runtime + "ms: " + stat);
                    // check
                    
                    this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).applyLessThanThreshold(
                            stat.getFreeMemory(),
                            UnitUtil.parse(executeCheck.getParameter("warning", "10MiB"), 5 * UnitUtil.Mi), 
                            UnitUtil.parse(executeCheck.getParameter("critical", "5MiB"), 10 * UnitUtil.Mi),
                            "Agent Memory: " + (stat.getFreeMemory() / UnitUtil.Mi) + " MiB of " + (stat.getMaxMemory() / UnitUtil.Mi) + " MiB free"
                    ).runtime(runtime));
                });
            }
            else
            {
                // raise an error
                this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
}
