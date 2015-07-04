package com.intrbiz.bergamot.worker.engine.agent;

import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.server.BergamotAgentServerHandler;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetCon;
import com.intrbiz.bergamot.model.message.agent.stat.NetConStat;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.worker.engine.AbstractExecutor;
import com.intrbiz.gerald.polyakov.gauge.IntegerGaugeReading;



/**
 * Check network connections on a Bergamot Agent
 */
public class NetConExecutor extends AbstractExecutor<AgentEngine>
{
    public static final String NAME = "network-connection";
    
    private Logger logger = Logger.getLogger(NetConExecutor.class);

    public NetConExecutor()
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
        if (logger.isTraceEnabled()) logger.trace("Checking Bergamot Agent network connections");
        try
        {
            // check the host presence
            UUID agentId = executeCheck.getAgentId();
            if (agentId == null) throw new RuntimeException("No agent id was given");
            // lookup the agent
            BergamotAgentServerHandler agent = this.getEngine().getAgentServer().getRegisteredAgent(agentId);
            if (agent != null)
            {
                // build the check
                CheckNetCon check = new CheckNetCon();
                check.setServer(executeCheck.getBooleanParameter("server", true));
                check.setClient(executeCheck.getBooleanParameter("client", false));
                check.setTcp(executeCheck.getBooleanParameter("tcp", true));
                check.setUdp(executeCheck.getBooleanParameter("udp", true));
                check.setUnix(executeCheck.getBooleanParameter("unix", false));
                check.setRaw(executeCheck.getBooleanParameter("raw", false));
                // ports
                check.setLocalPort(executeCheck.getIntParameter("local_port", 0));
                check.setRemotePort(executeCheck.getIntParameter("remote_port", 0));
                // addresses
                check.setLocalAddress(executeCheck.getParameter("local_address"));
                check.setRemoteAddress(executeCheck.getParameter("remote_address"));
                // get the process stats
                long sent = System.nanoTime();
                agent.sendMessageToAgent(check, (response) -> {
                    double runtime = ((double)(System.nanoTime() - sent)) / 1000_000D;
                    NetConStat stat = (NetConStat) response;
                    if (logger.isTraceEnabled()) logger.trace("Got net cons in " + runtime + "ms: " + stat);
                    // apply the check
                    this.publishActiveResult(executeCheck, new ActiveResultMO().fromCheck(executeCheck).applyRange(
                            stat.getConnections().size(),
                            executeCheck.getIntRangeParameter("warning",  new Integer[] {1, 1}), 
                            executeCheck.getIntRangeParameter("critical", new Integer[] {1, 1}), 
                            "found " + stat.getConnections().size() + " connections"
                    ).runtime(runtime));
                    // readings
                    this.publishReading(executeCheck, new IntegerGaugeReading("connections", null, stat.getConnections().size()));
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
