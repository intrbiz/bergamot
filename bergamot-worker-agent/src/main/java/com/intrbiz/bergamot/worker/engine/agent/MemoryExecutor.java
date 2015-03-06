package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.stat.MemStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

/**
 * Check the memory usage of a Bergamot Agent
 */
public class MemoryExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "memory";
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");
    
    private Logger logger = Logger.getLogger(MemoryExecutor.class);

    public MemoryExecutor()
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
        logger.debug("Checking Bergamot Agent Memory Usage, for agent: " + executeCheck.getParameter("agent_id"));
        try
        {
            // check the host presence
            UUID agentId = UUID.fromString(executeCheck.getParameter("agent_id"));
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // get the CPU stats
                agent.sendMessageToAgent(new CheckMem(), (response) -> {
                    MemStat stat = (MemStat) response;
                    logger.debug("Got Memory usage: " + stat);
                    // compute the result
                    ActiveResultMO result = new ActiveResultMO().fromCheck(executeCheck);
                    // check
                    result.ok("Memory: " + (stat.getActualUsedMemory() / (1024L * 1024L)) + " MiB of " + (stat.getTotalMemory() / (1024L * 1024L)) + " MiB (" + DFMT.format((((double) stat.getActualUsedMemory()) / ((double) stat.getTotalMemory())) * 100) + "%) used");
                    // submit
                    resultSubmitter.accept(result);
                });
            }
            else
            {
                // raise an error
                resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
}
