package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckNetIO;
import com.intrbiz.bergamot.model.message.agent.stat.NetIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.netio.NetIOInfo;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.DoubleGaugeReading;


/**
 * Check network IO for an interface via Bergamot Agent
 */
public class NetIOExecutor extends AbstractAgentExecutor<CheckNetIO, NetIOStat>
{
    public static final String NAME = "network-io";
    
    private Logger logger = Logger.getLogger(NetIOExecutor.class);
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");

    public NetIOExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckNetIO buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        return new CheckNetIO(executeCheck.getParameterCSV("interface"));
    }

    @Override
    protected void processResponse(NetIOStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got NetIOStat: " + stat);
        // thresholds
        double warning = executeCheck.getDoubleParameter("warning",  50);
        double critical = executeCheck.getDoubleParameter("critical", 75);
        // apply the check
        boolean peak = executeCheck.getBooleanParameter("peak", false);
        context.publishActiveResult(new ActiveResult().applyThresholds(
                stat.getIfaces(),
                (v, t) -> (peak ? v.getFiveMinuteRate().getTxPeakRateMbps(): v.getFiveMinuteRate().getTxRateMbps()) > t || (peak ? v.getFiveMinuteRate().getRxPeakRateMbps(): v.getFiveMinuteRate().getRxRateMbps()) > t,
                warning, 
                critical, 
                stat.getIfaces().stream().map((n) -> 
                    n.getName() + 
                    " Tx: " + DFMT.format(n.getFiveMinuteRate().getTxRateMbps()) + "Mb/s (" + DFMT.format(n.getFiveMinuteRate().getTxPeakRateMbps()) + "Mb/s Peak)" + 
                    " Rx: " + DFMT.format(n.getFiveMinuteRate().getRxRateMbps()) + "Mb/s (" + DFMT.format(n.getFiveMinuteRate().getRxPeakRateMbps()) + "Mb/s Peak)"
                ).collect(Collectors.joining("; "))
        ));
        // readings
        ReadingParcelMessage readings = new ReadingParcelMessage().fromCheck(executeCheck.getCheckId()).captured(System.currentTimeMillis());
        for (NetIOInfo iface : stat.getIfaces())
        {
            // rate
            readings.reading(new DoubleGaugeReading("rx-rate-[" + iface.getName() + "]", "Mb/s", iface.getFiveMinuteRate().getRxRateMbps(), warning, critical, null, null));
            readings.reading(new DoubleGaugeReading("rx-rate-peak-[" + iface.getName() + "]", "Mb/s", iface.getFiveMinuteRate().getRxPeakRateMbps(), warning, critical, null, null));
            readings.reading(new DoubleGaugeReading("tx-rate-[" + iface.getName() + "]", "Mb/s", iface.getFiveMinuteRate().getTxRateMbps(), warning, critical, null, null));
            readings.reading(new DoubleGaugeReading("tx-rate-peak-[" + iface.getName() + "]", "Mb/s", iface.getFiveMinuteRate().getTxPeakRateMbps(), warning, critical, null, null));
        }
        context.publishReading(readings);
    }
}
