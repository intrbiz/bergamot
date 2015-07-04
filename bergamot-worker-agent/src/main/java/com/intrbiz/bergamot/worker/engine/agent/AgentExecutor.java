package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckAgent;
import com.intrbiz.bergamot.model.message.agent.stat.AgentStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

/**
 * Check the internals of a Bergamot Agent
 */
public class AgentExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "agent";
    
    private Logger logger = Logger.getLogger(AgentExecutor.class);

    public AgentExecutor()
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
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent self");
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
                    this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).info(
                            stat.getAgentProduct() + " " + stat.getAgentVersion() + " (" + stat.getRuntime() + " [" + stat.getRuntimeVendor() + "] " + " " + stat.getRuntimeVersion() + ") on " + stat.getOsName() + " (" + stat.getOsArch() + ")"
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
