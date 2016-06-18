package com.intrbiz.bergamot.agent.handler;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckCPU;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.CPUStat;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUInfo;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUTime;
import com.intrbiz.bergamot.model.message.agent.stat.cpu.CPUUsage;

public class CPUInfoHandler extends AbstractAgentHandler
{
    private SigarProxy sigar = Humidor.getInstance().getSigar();

    public CPUInfoHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckCPU.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        try
        {
            CPUStat stat = new CPUStat(request);
            // cpu info
            CpuInfo[] infos = this.sigar.getCpuInfoList();
            stat.setCpuCount(infos.length);
            for (CpuInfo info : infos)
            {
                stat.getInfo().add(toInfo(info));
            }
            // cpu usage %
            stat.setTotalUsage(toUsage(this.sigar.getCpuPerc()));
            for (CpuPerc perc : this.sigar.getCpuPercList())
            {
                stat.getUsage().add(toUsage(perc));
            }
            // cpu time
            stat.setTotalTime(toTime(this.sigar.getCpu()));
            for (Cpu cpu : this.sigar.getCpuList())
            {
                stat.getTime().add(toTime(cpu));
            }
            // load
            double[] load = this.sigar.getLoadAverage();
            stat.getLoad().add(load[0]);
            stat.getLoad().add(load[1]);
            stat.getLoad().add(load[2]);
            //
            return stat;
        }
        catch (SigarException e)
        {
            return new GeneralError(e.getMessage());
        }
    }
    
    protected CPUInfo toInfo(CpuInfo info)
    {
        CPUInfo ret = new CPUInfo();
        ret.setVendor(info.getVendor());
        ret.setModel(info.getModel());
        ret.setSpeed(info.getMhz());
        return ret;
    }
    
    protected CPUUsage toUsage(CpuPerc perc)
    {
        CPUUsage ret = new CPUUsage();
        ret.setTotal(perc.getCombined());
        ret.setSystem(perc.getSys());
        ret.setUser(perc.getUser());
        ret.setWait(perc.getWait());
        return ret;
    }
    
    protected CPUTime toTime(Cpu time)
    {
        CPUTime ret = new CPUTime();
        ret.setTotal(time.getTotal());
        ret.setSystem(time.getSys());
        ret.setUser(time.getTotal());
        ret.setWait(time.getWait());
        return ret;
    }
}
