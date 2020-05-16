package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.stat.AgentStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;

/**
 * Check the internals of a Bergamot Agent
 */
public class AgentExecutor extends AbstractCheckExecutor<AgentEngine>
{
    public static final String NAME = "agent";
    
    private Logger logger = Logger.getLogger(AgentExecutor.class);

    public AgentExecutor()
    {
        super(NAME);
    }

    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent self");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                // get the Agent stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(new CheckAgent(), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    AgentStat stat = (AgentStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got agent info in " + runtime + "ms: " + stat);
                    context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).info(
                            stat.getAgentProduct() + " " + stat.getAgentVersion() + " (" + stat.getRuntime() + " [" + stat.getRuntimeVendor() + "] " + " " + stat.getRuntimeVersion() + ") on " + stat.getOsName() + " (" + stat.getOsArch() + ")"
                    ).runtime(runtime));
                });
            }
            else
            {
                // raise an error
                context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            context.publishActiveResult(new ActiveResult().fromCheck(executeCheck).error(e));
        }
    }
}
