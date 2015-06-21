package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckDiskIO;
import com.intrbiz.bergamot.model.message.agent.stat.DiskIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIOInfo;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;


/**
 * Check disk IO for an interface via Bergamot Agent
 */
public class DiskIOExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "disk-io";
    
    private Logger logger = Logger.getLogger(DiskIOExecutor.class);
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");

    public DiskIOExecutor()
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

    /**
     * Parameters:
     *   devices - CSV list of block device names (eg: /dev/sda1) to check, blank for all
     *   warning   - the throughput warning threshold in MB/s
     *   critical  - the throughput critical threshold in MB/s
     *   peak      - check the peak IO rate rather than 5 minute average
     */
    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent disk io");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // get the user stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(new CheckDiskIO(executeCheck.getParameterCSV("device")), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    DiskIOStat stat = (DiskIOStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got DiskIOStat in " + runtime + "ms: " + stat);
                    // thresholds
                    double warning = executeCheck.getDoubleParameter("warning",  200);
                    double critical = executeCheck.getDoubleParameter("critical", 250);
                    // apply the check
                    boolean peak = executeCheck.getBooleanParameter("peak", false);
                    resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).applyThresholds(
                            stat.getDisks(),
                            (v, t) -> (peak ? v.getFiveMinuteRate().getReadPeakRateMBps(): v.getFiveMinuteRate().getReadRateMBps()) > t || (peak ? v.getFiveMinuteRate().getWritePeakRateMBps(): v.getFiveMinuteRate().getWriteRateMBps()) > t,
                            warning, 
                            critical, 
                            stat.getDisks().stream().map((n) -> 
                                n.getName() + 
                                " Read: " + DFMT.format(n.getFiveMinuteRate().getReadRateMBps()) + "MB/s (" + DFMT.format(n.getFiveMinuteRate().getReadPeakRateMBps()) + "MB/s Peak) - " + DFMT.format(n.getFiveMinuteRate().getReads()) + "/s (" + DFMT.format(n.getFiveMinuteRate().getPeakReads()) + "/s Peak)" + 
                                " Write: " + DFMT.format(n.getFiveMinuteRate().getWriteRateMBps()) + "MB/s (" + DFMT.format(n.getFiveMinuteRate().getWritePeakRateMBps()) + "MB/s Peak) - " + DFMT.format(n.getFiveMinuteRate().getWrites()) + "/s (" + DFMT.format(n.getFiveMinuteRate().getPeakWrites()) + "/s Peak)"
                            ).collect(Collectors.joining("; "))
                    ).runtime(runtime));
                    // readings
                    ReadingParcelMO readings = new ReadingParcelMO().fromCheck(executeCheck.getCheckId()).captured(System.currentTimeMillis());
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
