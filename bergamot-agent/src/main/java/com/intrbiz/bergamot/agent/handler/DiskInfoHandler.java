package com.intrbiz.bergamot.agent.handler;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import com.intrbiz.bergamot.agent.AgentHandler;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckDisk;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;
import com.intrbiz.bergamot.model.message.agent.stat.DiskStat;
import com.intrbiz.bergamot.model.message.agent.stat.disk.DiskInfo;

public class DiskInfoHandler implements AgentHandler
{
    private SigarProxy sigar = Humidor.getInstance().getSigar();

    public DiskInfoHandler()
    {
        super();
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckDisk.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        try
        {
            DiskStat stat = new DiskStat(request);
            FileSystem[] fileSystems = sigar.getFileSystemList();
            for (FileSystem fs : fileSystems)
            {
                // TODO: bug in Sigar relating to BTRFS
                if (fs.getType() == 2 || "btrfs".equalsIgnoreCase(fs.getSysTypeName()))
                {
                    DiskInfo disk = new DiskInfo();
                    disk.setMount(fs.getDirName());
                    disk.setDevice(fs.getDevName());
                    disk.setType(fs.getSysTypeName());
                    FileSystemUsage usage = this.sigar.getFileSystemUsage(fs.getDirName());
                    disk.setSize(usage.getTotal() * 1024L);
                    disk.setAvailable(usage.getFree() * 1024L);
                    disk.setUsed(usage.getUsed() * 1024L);
                    disk.setUsedPercent(usage.getUsePercent() * 100D);
                    stat.getDisks().add(disk);
                }
            }
            return stat;
        }
        catch (SigarException e)
        {
            return new GeneralError(e.getMessage());
        }
    }
}
