package com.intrbiz.bergamot.agent.handler;

import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.NetConnection;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetCon;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.NetConStat;
import com.intrbiz.bergamot.model.message.agent.stat.netcon.NetConInfo;
import com.intrbiz.bergamot.util.AgentUtil;

public class NetConInfoHandler extends AbstractAgentHandler
{    
    private SigarProxy sigar = Humidor.getInstance().getSigar();

    public NetConInfoHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckNetCon.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        CheckNetCon check = (CheckNetCon) request;
        try
        {
            // flags
            int flags = 0;
            if (check.isClient()) flags |= NetFlags.CONN_CLIENT;
            if (check.isServer()) flags |= NetFlags.CONN_SERVER;
            if (check.isTcp()) flags |= NetFlags.CONN_TCP;
            if (check.isUdp()) flags |= NetFlags.CONN_UDP;
            if (check.isUnix()) flags |= NetFlags.CONN_UNIX;
            if (check.isRaw()) flags |= NetFlags.CONN_RAW;
            // get the stats
            NetConStat stat = new NetConStat(request);
            for (NetConnection con : this.sigar.getNetConnectionList(flags))
            {
                if (matchesFilter(check, con))
                {
                    NetConInfo info = new NetConInfo();
                    info.setProtocol(con.getTypeString());
                    info.setState(con.getStateString());
                    info.setLocalAddress(con.getLocalAddress());
                    info.setLocalPort(con.getLocalPort());
                    info.setRemoteAddress(con.getRemoteAddress());
                    info.setRemotePort(con.getRemotePort());
                    stat.getConnections().add(info);
                }
            }
            return stat;
        }
        catch (SigarException e)
        {
            return new GeneralError(e.getMessage());
        }
    }
    
    private static boolean matchesFilter(CheckNetCon check, NetConnection con)
    {
        // local port
        if (check.getLocalPort() > 0)
        {
            if (con.getLocalPort() != check.getLocalPort())
                return false;
        }
        // remote port
        if (check.getRemotePort() > 0)
        {
            if (con.getRemotePort() != check.getRemotePort())
                return false;
        }
        // local address
        if (! AgentUtil.isEmpty(check.getLocalAddress()))
        {
            if (! check.getLocalAddress().equalsIgnoreCase(con.getLocalAddress()))
                return false;
        }
        // remote address
        if (! AgentUtil.isEmpty(check.getRemoteAddress()))
        {
            if (! check.getRemoteAddress().equalsIgnoreCase(con.getRemoteAddress()))
                return false;
        }
        return true;
    }
}
