package com.intrbiz.bergamot.worker.engine.agent;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.stat.AgentStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.util.UnitUtil;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Check internal memory usage of a Bergamot Agent
 */
public class AgentMemoryExecutor extends AbstractAgentExecutor<CheckAgent, AgentStat>
{
    public static final String NAME = "agent-memory";
    
    private Logger logger = Logger.getLogger(AgentMemoryExecutor.class);

    public AgentMemoryExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckAgent buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        return new CheckAgent();
    }

    @Override
    protected void processResponse(AgentStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got agent info: " + stat);
        // check
        context.publishActiveResult(new ActiveResult().applyLessThanThreshold(
            stat.getFreeMemory(),
            UnitUtil.parse(executeCheck.getParameter("warning", "10MiB"), 5 * UnitUtil.Mi), 
            UnitUtil.parse(executeCheck.getParameter("critical", "5MiB"), 10 * UnitUtil.Mi),
            "Agent Memory: " + (stat.getFreeMemory() / UnitUtil.Mi) + " MiB of " + (stat.getMaxMemory() / UnitUtil.Mi) + " MiB free"
        ));
    }
}
