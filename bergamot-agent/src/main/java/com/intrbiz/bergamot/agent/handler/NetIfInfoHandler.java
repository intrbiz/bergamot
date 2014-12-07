package com.intrbiz.bergamot.agent.handler;

import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import com.intrbiz.bergamot.agent.AgentHandler;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIf;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.NetIfStat;
import com.intrbiz.bergamot.model.message.agent.stat.netif.NetIfInfo;

public class NetIfInfoHandler implements AgentHandler
{
    private SigarProxy sigar = Humidor.getInstance().getSigar();

    public NetIfInfoHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckNetIf.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        try
        {
            NetIfStat stat = new NetIfStat(request);
            stat.setHostname(this.sigar.getFQDN());
            for (String iface : this.sigar.getNetInterfaceList())
            {
                NetIfInfo info = new NetIfInfo();
                info.setName(iface);
                NetInterfaceConfig nic = this.sigar.getNetInterfaceConfig(iface);
                NetInterfaceStat nis = sigar.getNetInterfaceStat(iface);
                info.setHwAddress(nic.getHwaddr());
                info.setAddress(nic.getAddress());
                info.setNetmask(nic.getNetmask());
                info.setTxBytes(nis.getTxBytes());
                info.setRxBytes(nis.getRxBytes());
                stat.getIfaces().add(info);
            }
            return stat;
        }
        catch (SigarException e)
        {
            return new GeneralError(e.getMessage());
        }
    }
    
    protected static double round(double d)
    {
        return (((double) Math.round(d * 1000)) / 1000D);
    }
}