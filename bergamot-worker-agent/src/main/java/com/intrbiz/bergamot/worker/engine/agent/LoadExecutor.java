package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.stat.CPUStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;

/**
 * Check the load average of a Bergamot Agent
 */
public class LoadExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "load";
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");
    
    private Logger logger = Logger.getLogger(LoadExecutor.class);

    public LoadExecutor()
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
                    // thresholds
                    double load1Warning = executeCheck.getDoubleParameter("load_1_warning");
                    double load1critical = executeCheck.getDoubleParameter("load_1_critical");
                    double load5Warning = executeCheck.getDoubleParameter("load_5_warning", stat.getCpuCount());
                    double load5critical = executeCheck.getDoubleParameter("load_5_critical", stat.getCpuCount() * 1.5D);
                    double load15Warning = executeCheck.getDoubleParameter("load_15_warning");
                    double load15critical = executeCheck.getDoubleParameter("load_15_critical");
                    // apply the check
                    String message = "Load: " + DFMT.format(stat.getLoad1()) + ", " + DFMT.format(stat.getLoad5()) + ", " + DFMT.format(stat.getLoad15());
                    ActiveResultMO result = new ActiveResultMO().fromCheck(executeCheck).ok(message);
                    // check load 1
                    if (load1Warning > 0 && load1critical > 0)
                        result.applyGreaterThanThreshold(stat.getLoad1(), load1Warning, load1critical, message);
                    // check load 5
                    if (load5Warning > 0 && load5critical > 0)
                        result.applyGreaterThanThreshold(stat.getLoad1(), load5Warning, load5critical, message);
                    // check load 15
                    if (load15Warning > 0 && load15critical > 0)
                        result.applyGreaterThanThreshold(stat.getLoad15(), load15Warning, load15critical, message);
                    // publish
                    this.publishActiveResult(executeCheck, result.runtime(runtime));
                    // readings
                    this.publishReading(executeCheck, 
                        new DoubleGaugeReading("load-1",  null, stat.getLoad1(),  load1Warning  < 0 ? null : load1Warning,  load1critical  < 0 ? null : load1critical,  null, null),
                        new DoubleGaugeReading("load-5",  null, stat.getLoad5(),  load5Warning  < 0 ? null : load5Warning,  load5critical  < 0 ? null : load5critical,  null, null),
                        new DoubleGaugeReading("load-15", null, stat.getLoad15(), load15Warning < 0 ? null : load15Warning, load15critical < 0 ? null : load15critical, null, null)
                    );
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
