package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.stat.CPUStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.util.UnitUtil;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;

/**
 * Check the cpu usage of a Bergamot Agent
 */
public class CPUExecutor extends AbstractAgentExecutor<CheckCPU, CPUStat>
{
    public static final String NAME = "cpu";
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");
    
    private Logger logger = Logger.getLogger(CPUExecutor.class);

    public CPUExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckCPU buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        return new CheckCPU();
    }

    @Override
    protected void processResponse(CPUStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got CPU usage: " + stat);
        // apply the check
        context.publishActiveResult(new ActiveResult().applyGreaterThanThreshold(
                stat.getTotalUsage().getTotal(), 
                executeCheck.getPercentParameter("cpu_warning", 0.8D), 
                executeCheck.getPercentParameter("cpu_critical", 0.9D), 
                "Load: " + DFMT.format(stat.getLoad1()) + " " + DFMT.format(stat.getLoad5()) + " " + DFMT.format(stat.getLoad15()) + ", Usage: " + DFMT.format(UnitUtil.toPercent(stat.getTotalUsage().getTotal())) + "% of " + stat.getCpuCount() + " @ " + stat.getInfo().get(0).getSpeed() + " MHz " + stat.getInfo().get(0).getVendor() + " " + stat.getInfo().get(0).getModel()
        ));
        // readings
        context.publishReading(executeCheck, 
            new DoubleGaugeReading("cpu-usage-total", "%", UnitUtil.toPercent(stat.getTotalUsage().getTotal()), UnitUtil.toPercent(executeCheck.getPercentParameter("cpu_warning", 0.8D)), UnitUtil.toPercent(executeCheck.getPercentParameter("cpu_critical", 0.9D)), 1D, 100D),
            new DoubleGaugeReading("cpu-usage-system", "%", UnitUtil.toPercent(stat.getTotalUsage().getSystem()), null, null, 1D, 100D),
            new DoubleGaugeReading("cpu-usage-user", "%", UnitUtil.toPercent(stat.getTotalUsage().getUser()), null, null, 1D, 100D),
            new DoubleGaugeReading("cpu-usage-wait", "%", UnitUtil.toPercent(stat.getTotalUsage().getWait()), null, null, 1D, 100D)
        );
    }
}
