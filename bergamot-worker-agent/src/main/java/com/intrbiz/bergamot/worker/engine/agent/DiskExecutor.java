package com.intrbiz.bergamot.worker.engine.agent;

import static com.intrbiz.bergamot.util.UnitUtil.*;

import java.text.DecimalFormat;

import com.intrbiz.Util;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.stat.DiskStat;
import com.intrbiz.bergamot.model.message.agent.stat.disk.DiskInfo;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.util.UnitUtil;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;

/**
 * Check the disk usage of a mount point on a Bergamot Agent
 */
public class DiskExecutor extends AbstractAgentExecutor<CheckDisk, DiskStat>
{
    public static final String NAME = "disk";
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");

    public DiskExecutor()
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
        String mount = executeCheck.getParameter("mount");
        if (Util.isEmpty(mount)) throw new RuntimeException("No disk mount point was given");
        // find the mount
        DiskInfo disk = stat.getDisks().stream().filter((di) -> { return mount.equals(di.getMount()); }).findFirst().orElse(null);
        if (disk != null)
        {
            // apply the check
            double warning = executeCheck.getPercentParameter("warning", 0.8D);
            double critical = executeCheck.getPercentParameter("critical", 0.9D);
            context.publishActiveResult(new ActiveResult().applyGreaterThanThreshold(
                    fromPercent(disk.getUsedPercent()), 
                    warning,
                    critical,
                    "Disk: " + disk.getMount() + " " + disk.getMessageType() + " on " + disk.getDevice() +" " + DFMT.format(toG(disk.getUsed())) + " GB of " + DFMT.format(toG(disk.getSize())) + " GB (" + DFMT.format(disk.getUsedPercent()) + " %) used" 
            ));
            // readings
            context.publishReading(executeCheck, 
                new DoubleGaugeReading("disk-space-used-[" + disk.getMount() + "]", "MB", UnitUtil.toM(disk.getUsed()), UnitUtil.toM(disk.getSize()) * warning, UnitUtil.toM(disk.getSize()) * critical, 0D, UnitUtil.toM(disk.getSize())),
                new DoubleGaugeReading("disk-space-available-[" + disk.getMount() + "]", "MB", UnitUtil.toM(disk.getAvailable()), UnitUtil.toM(disk.getSize()) * (1D - warning), UnitUtil.toM(disk.getSize()) * (1D - critical), 0D, UnitUtil.toM(disk.getSize())),
                new DoubleGaugeReading("disk-space-used-percent-[" + disk.getMount() + "]", "%", disk.getUsedPercent(), UnitUtil.toPercent(warning), UnitUtil.toPercent(critical), 0D, 100D)
            );
        }
        else
        {
            context.publishActiveResult(new ActiveResult().error("No such mount point: " + mount));
        }
    }    
}
