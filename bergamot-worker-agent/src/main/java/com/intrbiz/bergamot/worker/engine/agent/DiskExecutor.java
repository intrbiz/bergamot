package com.intrbiz.bergamot.worker.engine.agent;

import static com.intrbiz.bergamot.util.UnitUtil.*;

import java.text.DecimalFormat;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.stat.DiskStat;
import com.intrbiz.bergamot.model.message.agent.stat.disk.DiskInfo;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.util.UnitUtil;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;

/**
 * Check the disk usage of a mount point on a Bergamot Agent
 */
public class DiskExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "disk";
    
    private Logger logger = Logger.getLogger(DiskExecutor.class);
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");

    public DiskExecutor()
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
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent Disk Usage");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            String mount = executeCheck.getParameter("mount");
            if (Util.isEmpty(mount)) throw new RuntimeException("No disk mount point was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // get the CPU stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(new CheckDisk(), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    DiskStat stat = (DiskStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got Disk usage in + " + runtime + "ms: " + stat);
                    // find the mount
                    DiskInfo disk = stat.getDisks().stream().filter((di) -> { return mount.equals(di.getMount()); }).findFirst().orElse(null);
                    if (disk != null)
                    {
                        // apply the check
                        double warning = executeCheck.getPercentParameter("warning", 0.8D);
                        double critical = executeCheck.getPercentParameter("critical", 0.9D);
                        this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).applyGreaterThanThreshold(
                                fromPercent(disk.getUsedPercent()), 
                                warning,
                                critical,
                                "Disk: " + disk.getMount() + " " + disk.getType() + " on " + disk.getDevice() +" " + DFMT.format(toG(disk.getUsed())) + " GB of " + DFMT.format(toG(disk.getSize())) + " GB (" + DFMT.format(disk.getUsedPercent()) + " %) used" 
                        ).runtime(runtime));
                        // readings
                        this.publishReading(executeCheck, 
                            new DoubleGaugeReading("disk-space-used-[" + disk.getMount() + "]", "MB", UnitUtil.toM(disk.getUsed()), UnitUtil.toM(disk.getSize()) * warning, UnitUtil.toM(disk.getSize()) * critical, 0D, UnitUtil.toM(disk.getSize())),
                            new DoubleGaugeReading("disk-space-available-[" + disk.getMount() + "]", "MB", UnitUtil.toM(disk.getAvailable()), UnitUtil.toM(disk.getSize()) * (1D - warning), UnitUtil.toM(disk.getSize()) * (1D - critical), 0D, UnitUtil.toM(disk.getSize())),
                            new DoubleGaugeReading("disk-space-used-percent-[" + disk.getMount() + "]", "%", disk.getUsedPercent(), UnitUtil.toPercent(warning), UnitUtil.toPercent(critical), 0D, 100D)
                        );
                    }
                    else
                    {
                        this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).error("No such mount point: " + mount).runtime(runtime));
                    }
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
