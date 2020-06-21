package com.intrbiz.bergamot.worker.engine.agent;

import static com.intrbiz.bergamot.util.UnitUtil.*;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.stat.DiskStat;
import com.intrbiz.bergamot.model.message.agent.stat.disk.DiskInfo;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.util.UnitUtil;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;



/**
 * Check the disk usage of a Bergamot Agent
 */
public class DisksExecutor extends AbstractAgentExecutor<CheckDisk, DiskStat>
{
    public static final String NAME = "disks";
    
    private Logger logger = Logger.getLogger(DisksExecutor.class);
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");

    public DisksExecutor()
    {
        super(NAME);
    }
    
    @Override
    protected CheckDisk buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        return new CheckDisk();
    }

    @Override
    protected void processResponse(DiskStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got Disk usage: " + stat);
        // apply the check
        double warning = executeCheck.getPercentParameter("warning", 0.8D);
        double critical = executeCheck.getPercentParameter("critical", 0.9D);
        context.publishActiveResult(new ActiveResult().applyGreaterThanThresholds(
                stat.getDisks().stream().map(DiskInfo::getUsedPercent).map(UnitUtil::fromPercent).collect(Collectors.toList()),
                warning,
                critical,
                "Disks: " + stat.getDisks().stream().map((disk)-> {
                    return "" + disk.getMount() + " " + DFMT.format(toG(disk.getUsed())) + " GB of " + DFMT.format(toG(disk.getSize())) + " GB (" + DFMT.format(disk.getUsedPercent()) + " %) used";
                }).collect(Collectors.joining("; "))
        ));
        // readings
        ReadingParcelMessage readings = new ReadingParcelMessage().fromCheck(executeCheck.getCheckId()).captured(System.currentTimeMillis());
        for (DiskInfo disk : stat.getDisks())
        {
            readings.reading(new DoubleGaugeReading("disk-space-used-[" + disk.getMount() + "]", "MB", UnitUtil.toM(disk.getUsed()), UnitUtil.toM(disk.getSize()) * warning, UnitUtil.toM(disk.getSize()) * critical, 0D, UnitUtil.toM(disk.getSize())));
            readings.reading(new DoubleGaugeReading("disk-space-available-[" + disk.getMount() + "]", "MB", UnitUtil.toM(disk.getAvailable()), UnitUtil.toM(disk.getSize()) * (1D - warning), UnitUtil.toM(disk.getSize()) * (1D - critical), 0D, UnitUtil.toM(disk.getSize()) ));
            readings.reading(new DoubleGaugeReading("disk-space-used-percent-[" + disk.getMount() + "]", "%", disk.getUsedPercent(), UnitUtil.toPercent(warning), UnitUtil.toPercent(critical), 0D, 100D));
        }
        context.publishReading(readings);
    }
}
