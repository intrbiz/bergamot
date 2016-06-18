package com.intrbiz.bergamot.agent.handler;

import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckMem;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.MemStat;

public class MemInfoHandler extends AbstractAgentHandler
{
    private SigarProxy sigar = Humidor.getInstance().getSigar();

    public MemInfoHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckMem.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        try
        {
            MemStat stat = new MemStat(request);
            Mem mem = this.sigar.getMem();
            stat.setRam(mem.getRam());
            stat.setTotalMemory(mem.getTotal());
            stat.setActualFreeMemory(mem.getActualFree());
            stat.setActualUsedMemory(mem.getActualUsed());
            stat.setFreeMemory(mem.getFree());
            stat.setUsedMemory(mem.getUsed());
            stat.setFreeMemoryPercentage(round(mem.getFreePercent()));
            stat.setUsedMemoryPercentage(round(mem.getUsedPercent()));
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
