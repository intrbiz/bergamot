package com.intrbiz.bergamot.worker.engine.agent;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckProcess;
import com.intrbiz.bergamot.model.message.agent.stat.ProcessStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.LongGaugeReading;


/**
 * Check process stats via Bergamot Agent
 */
public class ProcessStatsExecutor extends AbstractAgentExecutor<CheckProcess, ProcessStat>
{
    public static final String NAME = "process-stats";
    
    private Logger logger = Logger.getLogger(ProcessStatsExecutor.class);

    public ProcessStatsExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckProcess buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        // get the process stats
        CheckProcess check = new CheckProcess();
        check.setListProcesses(false);
        return check;
    }

    @Override
    protected void processResponse(ProcessStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got stat: " + stat);
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
        context.publishActiveResult(new ActiveResult().applyGreaterThanThreshold(
                count, 
                warning, 
                critical, 
                message
        ));
        // publish readings
        context.publishReading(executeCheck, new LongGaugeReading(state + "-processes", null, count, warning, critical, 0L, null));
    }
}
