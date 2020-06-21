package com.intrbiz.bergamot.worker.engine.agent;

import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.check.CheckNetCon;
import com.intrbiz.bergamot.model.message.agent.stat.NetConStat;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.worker.engine.CheckExecutionContext;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;



/**
 * Check listening port on a Bergamot Agent
 */
public class PortListenerExecutor extends AbstractAgentExecutor<CheckNetCon, NetConStat>
{
    public static final String NAME = "port-listener";
    
    private Logger logger = Logger.getLogger(PortListenerExecutor.class);

    public PortListenerExecutor()
    {
        super(NAME);
    }

    @Override
    protected CheckNetCon buildRequest(ExecuteCheck executeCheck, CheckExecutionContext context)
    {
     // build the check
        CheckNetCon check = new CheckNetCon();
        check.setServer(true);
        check.setClient(false);
        check.setTcp(executeCheck.getBooleanParameter("tcp",   true));
        check.setUdp(executeCheck.getBooleanParameter("udp",   true));
        check.setUnix(executeCheck.getBooleanParameter("unix", true));
        check.setRaw(executeCheck.getBooleanParameter("raw",   true));
        // ports
        check.setLocalPort(executeCheck.getIntParameter("local_port", 0));
        // addresses
        check.setLocalAddress(executeCheck.getParameter("local_address"));
        return check;
    }

    @Override
    protected void processResponse(NetConStat stat, ExecuteCheck executeCheck, CheckExecutionContext context)
    {
        if (logger.isTraceEnabled()) logger.trace("Got net cons: " + stat);
        // apply the check
        context.publishActiveResult(new ActiveResult().applyRange(
                stat.getConnections().size(),
                executeCheck.getIntRangeParameter("warning",  new Integer[] {1, 1}), 
                executeCheck.getIntRangeParameter("critical", new Integer[] {1, 1}), 
                "found " + stat.getConnections().size() + " listeners: " + stat.getConnections().stream().map((nc) -> nc.getProtocol() + " " + nc.getLocalAddress() + ":" + nc.getLocalPort()).collect(Collectors.joining(", "))
        ));
        // readings
        context.publishReading(executeCheck, new IntegerGaugeReading("connections", null, stat.getConnections().size()));
    }
}
