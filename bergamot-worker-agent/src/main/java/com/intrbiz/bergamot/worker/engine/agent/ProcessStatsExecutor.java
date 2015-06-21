package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.stat.ProcessStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;


/**
 * Check process stats via Bergamot Agent
 */
public class ProcessStatsExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "process-stats";
    
    private Logger logger = Logger.getLogger(ProcessStatsExecutor.class);

    public ProcessStatsExecutor()
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
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent process stats");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // get the process stats
                CheckProcess check = new CheckProcess();
                check.setListProcesses(false);
                long sent = System.nanoTime();
                agent.sendMessageToAgent(check, (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    ProcessStat stat = (ProcessStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got who in " + runtime + "ms: " + stat);
                    // thresholds
                    long warning = executeCheck.getLongParameter("warning",  150);
                    long critical = executeCheck.getLongParameter("critical", 200);
                    // get the metric
                    String state = executeCheck.getParameter("state", "total").toLowerCase();
                    long count = -1;
                    String message = null;
                    if ("running".equals(state))
                    {
                        count = stat.getRunning();
                        message = count + " running of " + stat.getTotal() + " processes";
                    }
                    else if ("sleeping".equals(state))
                    {
                        count = stat.getSleeping();
                        message = count + " sleeping of " + stat.getTotal() + " processes";
                    }
                    else if ("stopped".equals(state))
                    {
                        count = stat.getStopped();
                        message = count + " stopped of " + stat.getTotal() + " processes";
                    }
                    else if ("idle".equals(state))
                    {
                        count = stat.getIdle();
                        message = count + " idle of " + stat.getTotal() + " processes";
                    }
                    else if ("zombie".equals(state))
                    {
                        count = stat.getZombie();
                        message = count + " zombie of " + stat.getTotal() + " processes";
                    }
                    else if ("threads".equals(state))
                    {
                        count = stat.getThreads();
                        message = count + " total threads over " + stat.getTotal() + " total processes";
                    }
                    else
                    {
                        state = "total";
                        count = stat.getTotal();
                        message = count + " total processes, " + stat.getThreads() + " total threads";
                    }
                    // apply the check
                    resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).applyGreaterThanThreshold(
                            count, 
                            warning, 
                            critical, 
                            message
                    ).runtime(runtime));
                    // publish readings
                    // readings
                    ReadingParcelMO readings = new ReadingParcelMO().fromCheck(executeCheck.getCheckId()).captured(System.currentTimeMillis());
                    readings.reading(new LongGaugeReading(state + "-processes", null, count, warning, critical, 0L, null));
                    this.publishReading(new ReadingKey(executeCheck.getSiteId(), executeCheck.getProcessingPool()), readings);
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
