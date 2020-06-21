package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.stat.MemStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.util.UnitUtil;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;

/**
 * Check the memory usage of a Bergamot Agent
 */
public class MemoryExecutor extends AbstractAgentExecutor<CheckMem, MemStat>
{
    public static final String NAME = "memory";
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");
    
    private Logger logger = Logger.getLogger(MemoryExecutor.class);

    public MemoryExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckMem buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        return new CheckMem();
    }

    @Override
    protected void processResponse(MemStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got Memory usage: " + stat);
        // check
        context.publishActiveResult(new ActiveResult().applyGreaterThanThreshold(
                UnitUtil.toRatio((executeCheck.getBooleanParameter("ignore_caches", true) ? stat.getActualUsedMemory() : stat.getUsedMemory()), stat.getTotalMemory()),
                executeCheck.getPercentParameter("warning", 0.8F), 
                executeCheck.getPercentParameter("critical", 0.9F),
                "Memory: " + (stat.getActualUsedMemory() / UnitUtil.Mi) + " MiB of " + (stat.getTotalMemory() / UnitUtil.Mi) + " MiB (" + DFMT.format(UnitUtil.toPercent(stat.getActualUsedMemory(), stat.getTotalMemory())) + "%) used " + ((stat.getUsedMemory() - stat.getActualUsedMemory()) / UnitUtil.Mi) + " MiB caches"
        ));
        // readings
        double warning  = (executeCheck.getPercentParameter("warning", 0.8F) * stat.getTotalMemory()) / UnitUtil.Mi;
        double critical = (executeCheck.getPercentParameter("critical", 0.9F) * stat.getTotalMemory()) / UnitUtil.Mi;
        context.publishReading(executeCheck, 
            new DoubleGaugeReading("actual-used", "MiB", (double) (stat.getActualUsedMemory() / UnitUtil.Mi), warning, critical, 0D, (double) (stat.getTotalMemory()  / UnitUtil.Mi)),
            new DoubleGaugeReading("used", "MiB", (double) (stat.getUsedMemory() / UnitUtil.Mi), warning, critical, 0D, (double) (stat.getTotalMemory()  / UnitUtil.Mi))
        );
    }
}
