package com.intrbiz.bergamot.agent.handler;

import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.Uptime;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckUptime;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.UptimeStat;

public class UptimeInfoHandler extends AbstractAgentHandler
{
    private SigarProxy sigar = Humidor.getInstance().getSigar();

    public UptimeInfoHandler()
    {
        super();
    }

    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] { CheckUptime.class };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        try
        {
            UptimeStat stat = new UptimeStat(request);
            Uptime u = sigar.getUptime();
            stat.setUptime(u.getUptime());
            return stat;
        }
        catch (SigarException e)
        {
            return new GeneralError(e.getMessage());
        }
    }
}
