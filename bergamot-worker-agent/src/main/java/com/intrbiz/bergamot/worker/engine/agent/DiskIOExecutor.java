package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckDiskIO;
import com.intrbiz.bergamot.model.message.agent.stat.DiskIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIOInfo;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.AbstractCheckExecutor;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;


/**
 * Check disk IO for an interface via Bergamot Agent
 */
public class DiskIOExecutor extends AbstractCheckExecutor<AgentEngine>
{
    public static final String NAME = "disk-io";
    
    private Logger logger = Logger.getLogger(DiskIOExecutor.class);
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");

    public DiskIOExecutor()
    {
        super(NAME);
    }

    /**
     * Parameters:
     *   devices - CSV list of block device names (eg: /dev/sda1) to check, blank for all
     *   warning   - the throughput warning threshold in MB/s
     *   critical  - the throughput critical threshold in MB/s
     *   peak      - check the peak IO rate rather than 5 minute average
     */
    @Override
    public void execute(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent disk io");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getAgent(agentId);
            if (agent != null)
            {
                // get the user stats
                agent.sendMessageToAgent(new CheckDiskIO(executeCheck.getParameterCSV("device")), (response) -> {
                    DiskIOStat stat = (DiskIOStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got DiskIOStat in: " + stat);
                    // thresholds
                    double warning = executeCheck.getDoubleParameter("warning",  200);
                    double critical = executeCheck.getDoubleParameter("critical", 250);
                    // apply the check
                    boolean peak = executeCheck.getBooleanParameter("peak", false);
                    context.publishActiveResult(new ActiveResult().applyThresholds(
                            stat.getDisks(),
                            (v, t) -> (peak ? v.getFiveMinuteRate().getReadPeakRateMBps(): v.getFiveMinuteRate().getReadRateMBps()) > t || (peak ? v.getFiveMinuteRate().getWritePeakRateMBps(): v.getFiveMinuteRate().getWriteRateMBps()) > t,
                            warning, 
                            critical, 
                            stat.getDisks().stream().map((n) -> 
                                n.getName() + 
                                " Read: " + DFMT.format(n.getFiveMinuteRate().getReadRateMBps()) + "MB/s (" + DFMT.format(n.getFiveMinuteRate().getReadPeakRateMBps()) + "MB/s Peak) - " + DFMT.format(n.getFiveMinuteRate().getReads()) + "/s (" + DFMT.format(n.getFiveMinuteRate().getPeakReads()) + "/s Peak)" + 
                                " Write: " + DFMT.format(n.getFiveMinuteRate().getWriteRateMBps()) + "MB/s (" + DFMT.format(n.getFiveMinuteRate().getWritePeakRateMBps()) + "MB/s Peak) - " + DFMT.format(n.getFiveMinuteRate().getWrites()) + "/s (" + DFMT.format(n.getFiveMinuteRate().getPeakWrites()) + "/s Peak)"
                            ).collect(Collectors.joining("; "))
                    ));
                    // readings
                    ReadingParcelMessage readings = new ReadingParcelMessage().fromCheck(executeCheck.getCheckId()).captured(System.currentTimeMillis());
                    for (DiskIOInfo disk : stat.getDisks())
                    {
                        // rate
                        readings.reading(new DoubleGaugeReading("read-rate-[" + disk.getName() + "]", "MB/s", disk.getFiveMinuteRate().getReadRateMBps(), warning, critical, null, null));
                        readings.reading(new DoubleGaugeReading("read-rate-peak-[" + disk.getName() + "]", "MB/s", disk.getFiveMinuteRate().getReadPeakRateMBps(), warning, critical, null, null));
                        readings.reading(new DoubleGaugeReading("write-rate-[" + disk.getName() + "]", "MB/s", disk.getFiveMinuteRate().getWriteRateMBps(), warning, critical, null, null));
                        readings.reading(new DoubleGaugeReading("write-rate-peak-[" + disk.getName() + "]", "MB/s", disk.getFiveMinuteRate().getWritePeakRateMBps(), warning, critical, null, null));
                        // reads
                        readings.reading(new DoubleGaugeReading("reads-[" + disk.getName() + "]", null, disk.getFiveMinuteRate().getReads(), warning, critical, null, null));
                        readings.reading(new DoubleGaugeReading("writes-[" + disk.getName() + "]", null, disk.getFiveMinuteRate().getWrites(), warning, critical, null, null));
                    }
                    context.publishReading(readings);
                });
            }
            else
            {
                // raise an error
                context.publishActiveResult(new ActiveResult().disconnected("Bergamot Agent disconnected"));
            }
        }
        catch (Exception e)
        {
            context.publishActiveResult(new ActiveResult().error(e));
        }
    }
}
