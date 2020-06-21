package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckUptime;
import com.intrbiz.bergamot.model.message.agent.stat.UptimeStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;

/**
 * Check the uptime of a Bergamot Agent
 */
public class UptimeExecutor extends AbstractAgentExecutor<CheckUptime, UptimeStat>
{
    public static final String NAME = "uptime";
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");
    
    private Logger logger = Logger.getLogger(UptimeExecutor.class);

    public UptimeExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckUptime buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        return new CheckUptime();
    }

    @Override
    protected void processResponse(UptimeStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got Uptime: " + stat);
        // thresholds
        long critical = executeCheck.getLongParameter("critical", TimeUnit.MINUTES.toSeconds(5));
        long warning  = executeCheck.getLongParameter("warning",  TimeUnit.MINUTES.toSeconds(10));
        // the result
        ActiveResult result = new ActiveResult();
        // apply the check
        if (stat.getUptime() <= warning)
        {
            result.warning("Uptime: " + formatUptime(stat.getUptime()));
        }
        else if (stat.getUptime() <= critical)
        {
            result.critical("Uptime: " + formatUptime(stat.getUptime()));
        }
        else
        {
            result.ok("Up " + formatUptime(stat.getUptime()));
        }
        // submit
        context.publishActiveResult(result);
        // readings
        context.publishReading(executeCheck, new DoubleGaugeReading("uptime", "s", stat.getUptime()));
    }
    
    private String formatUptime(double uptimeDouble)
    {
        long uptime  = (long) uptimeDouble;
        long seconds = uptime % 60L;
        long minutes = (uptime / 60L) % 60L;
        long hours   = (uptime / 3600L) % 24L;
        long days    = uptime / 86400L;
        //
        return days + " day" + (days > 1 ? "s" : "") + ", " + hours + ":" + minutes + ":" + seconds + " (" + DFMT.format(uptimeDouble) + ")"; 
    }
}
