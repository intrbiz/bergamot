package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.stat.CPUStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.util.UnitUtil;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;

/**
 * Check the cpu usage of a Bergamot Agent
 */
public class CPUExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "cpu";
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");
    
    private Logger logger = Logger.getLogger(CPUExecutor.class);

    public CPUExecutor()
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
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent CPU Usage");
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
                agent.sendMessageToAgent(new CheckCPU(), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    CPUStat stat = (CPUStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got CPU usage in " + runtime + "ms: " + stat);
                    // apply the check
                    resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).applyGreaterThanThreshold(
                            stat.getTotalUsage().getTotal(), 
                            executeCheck.getPercentParameter("cpu_warning", 0.8D), 
                            executeCheck.getPercentParameter("cpu_critical", 0.9D), 
                            "Load: " + DFMT.format(stat.getLoad1()) + " " + DFMT.format(stat.getLoad5()) + " " + DFMT.format(stat.getLoad15()) + ", Usage: " + DFMT.format(UnitUtil.toPercent(stat.getTotalUsage().getTotal())) + "% of " + stat.getCpuCount() + " @ " + stat.getInfo().get(0).getSpeed() + " MHz " + stat.getInfo().get(0).getVendor() + " " + stat.getInfo().get(0).getModel()
                    ).runtime(runtime));
                });
            }
            else
            {
                // raise an error
                resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).error(e));
        }
    }
}
