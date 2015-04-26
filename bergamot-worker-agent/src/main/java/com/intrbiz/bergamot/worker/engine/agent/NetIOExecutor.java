package com.intrbiz.bergamot.worker.engine.agent;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIO;
import com.intrbiz.bergamot.model.message.agent.stat.NetIOStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;


/**
 * Check network IO for an interface via Bergamot Agent
 */
public class NetIOExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "network-io";
    
    private Logger logger = Logger.getLogger(NetIOExecutor.class);
    
    private static final DecimalFormat DFMT = new DecimalFormat("0.00");

    public NetIOExecutor()
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
     *   interface - CSV list of interface names to check, blank for all
     *   warning   - the throughput warning threshold in Mb/s
     *   critical  - the throughput critical threshold in Mb/s
     */
    @Override
    public void execute(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter)
    {
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent network io");
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
                agent.sendMessageToAgent(new CheckNetIO(executeCheck.getParameterCSV("interface")), (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    NetIOStat stat = (NetIOStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got NetIOStat in " + runtime + "ms: " + stat);
                    // apply the check
                    boolean peak = executeCheck.getBooleanParameter("peak", false);
                    resultSubmitter.accept(new ActiveResultMO().fromCheck(executeCheck).applyThresholds(
                            stat.getIfaces(),
                            (v, t) -> (peak ? v.getFiveMinuteRate().getTxPeakRateMbps(): v.getFiveMinuteRate().getTxRateMbps()) > t || (peak ? v.getFiveMinuteRate().getRxPeakRateMbps(): v.getFiveMinuteRate().getRxRateMbps()) > t,
                            executeCheck.getDoubleParameter("warning",  50), 
                            executeCheck.getDoubleParameter("critical", 75), 
                            stat.getIfaces().stream().map((n) -> 
                                n.getName() + 
                                " Tx: " + DFMT.format(n.getFiveMinuteRate().getTxRateMbps()) + "Mb/s (" + DFMT.format(n.getFiveMinuteRate().getTxPeakRateMbps()) + "Mb/s Peak)" + 
                                " Rx: " + DFMT.format(n.getFiveMinuteRate().getRxRateMbps()) + "Mb/s (" + DFMT.format(n.getFiveMinuteRate().getRxPeakRateMbps()) + "Mb/s Peak)"
                            ).collect(Collectors.joining("; "))
                    ).runtime(runtime));
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
