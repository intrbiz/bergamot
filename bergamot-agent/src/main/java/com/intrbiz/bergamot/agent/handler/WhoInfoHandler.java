package com.intrbiz.bergamot.agent.handler;

import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.Who;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckWho;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.WhoStat;
import com.intrbiz.bergamot.model.message.agent.stat.who.WhoInfo;

public class WhoInfoHandler extends AbstractAgentHandler
{    
    private SigarProxy sigar = Humidor.getInstance().getSigar();

    public WhoInfoHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckWho.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        try
        {
            WhoStat stat = new WhoStat(request);
            for (Who who : this.sigar.getWhoList())
            {
                WhoInfo info = new WhoInfo();
                info.setUser(who.getUser());
                info.setHost(who.getHost());
                info.setDevice(who.getDevice());
                info.setTime(who.getTime());
                stat.getUsers().add(info);
            }
            return stat;
        }
        catch (SigarException e)
        {
            return new GeneralError(e.getMessage());
        }
    }
}
