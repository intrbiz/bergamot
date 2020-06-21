package com.intrbiz.bergamot.worker.engine.agent;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.stat.AgentStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Check the internals of a Bergamot Agent
 */
public class AgentExecutor extends AbstractAgentExecutor<CheckAgent, AgentStat>
{
    public static final String NAME = "agent";
    
    private Logger logger = Logger.getLogger(AgentExecutor.class);

    public AgentExecutor()
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
        context.publishActiveResult(new ActiveResult().info(
            stat.getAgentProduct() + " " + stat.getAgentVersion() + " (" + stat.getRuntime() + " [" + stat.getRuntimeVendor() + "] " + " " + stat.getRuntimeVersion() + ") on " + stat.getOsName() + " (" + stat.getOsArch() + ")"
        ));
    }
}
