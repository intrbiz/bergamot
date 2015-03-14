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
import com.intrbiz.bergamot.util.UnitUtil;
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
        logger.trace("Checking Bergamot Agent Memory Usage");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // get the CPU stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(new CheckMem(), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    MemStat stat = (MemStat) response;
                    logger.trace("Got Memory usage in " + runtime + "ms: " + stat);
                    // check
                    resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).applyThreshold(
                            UnitUtil.toRatio((executeCheck.getBooleanParameter("ignore_caches", true) ? stat.getActualUsedMemory() : stat.getUsedMemory()), stat.getTotalMemory()),
                            executeCheck.getPercentParameter("warning", 0.8F), 
                            executeCheck.getPercentParameter("critical", 0.9F),
                            "Memory: " + (stat.getActualUsedMemory() / UnitUtil.Mi) + " MiB of " + (stat.getTotalMemory() / UnitUtil.Mi) + " MiB (" + DFMT.format(UnitUtil.toPercent(stat.getActualUsedMemory(), stat.getTotalMemory())) + "%) used " + ((stat.getUsedMemory() - stat.getActualUsedMemory()) / UnitUtil.Mi) + " MiB caches"
                    ).runtime(runtime));
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
