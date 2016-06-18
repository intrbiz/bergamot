package com.intrbiz.bergamot.agent.handler;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.hyperic.sigar.DiskUsage;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import com.intrbiz.bergamot.agent.util.SampleRingBuffer;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckDiskIO;
import com.intrbiz.bergamot.model.message.agent.stat.DiskIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIOInfo;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIORateInfo;

public class DiskIOHandler extends AbstractAgentHandler
{
    private Logger logger = Logger.getLogger(DiskIOHandler.class);
    
    private final SigarProxy sigar = Humidor.getInstance().getSigar();
    
    private final Timer timer = new Timer();
    
    private final Map<String, DiskIO> registeredDevices = new ConcurrentHashMap<String, DiskIO>();
    
    private final Map<String, String> registeredMounts = new ConcurrentHashMap<String, String>();

    public DiskIOHandler()
    {
        super();
        // register polling of known disks
        this.setupDiskSampling();
        // periodically setup disks
        this.timer.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                setupDiskSampling();
            }
        }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1));
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckDiskIO.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        CheckDiskIO check = (CheckDiskIO) request;
        DiskIOStat stat = new DiskIOStat(request);
        // get stats for our disks
        for (String name : (check.getDevices() == null || check.getDevices().isEmpty() ? this.registeredDevices.keySet() : check.getDevices()))
        {
            // lookup by device
            DiskIO disk = this.registeredDevices.get(name);
            // lookup by mount?
            if (disk == null)
            {
                String device = this.registeredMounts.get(name);
                if (device != null)
                {
                    disk = this.registeredDevices.get(device);
                }
            }
            // compute
            if (disk != null)
            {
                DiskIOInfo info = new DiskIOInfo(name);
                info.setInstantRate(disk.instantRate());
                info.setFiveMinuteRate(disk.fiveMinuteRate());
                stat.getDisks().add(info);
            }
        }
        return stat;
    }
    
    protected void setupDiskSampling()
    {
        synchronized (this)
        {
            try
            {
                Set<FileSystem> devices = new HashSet<FileSystem>(Arrays.asList(this.sigar.getFileSystemList()));
                Set<String> deviceNames = new TreeSet<String>();
                // add interfaces not yet setup
                SecureRandom random = new SecureRandom();
                for (FileSystem device : devices)
                {
                    // TODO: bug in Sigar relating to BTRFS
                    if (device.getType() == 2 || "btrfs".equalsIgnoreCase(device.getSysTypeName()))
                    {
                        deviceNames.add(device.getDevName());
                        if (! this.registeredDevices.containsKey(device.getDevName()))
                        {
                            // setup - 5 minute history sampled every 5 seconds;
                            DiskIO diskIO = new DiskIO(device.getDevName(), 60, 5L, TimeUnit.SECONDS);
                            // register
                            this.registeredDevices.put(device.getDevName(), diskIO);
                            // schedule
                            long skew = ((long) (diskIO.interval * random.nextDouble()));
                            this.timer.scheduleAtFixedRate(diskIO, skew, diskIO.interval);
                            logger.info("Starting sampling of disk IO for device: " + device.getDevName() + " every 5 seconds");
                        }
                        this.registeredMounts.put(device.getDirName(), device.getDevName());
                    }
                }
                // remove any interfaces which have gone away
                for (Iterator<Entry<String, DiskIO>> i = this.registeredDevices.entrySet().iterator(); i.hasNext(); )
                {
                    Entry<String, DiskIO> entry = i.next();
                    // remove ?
                    if (! deviceNames.contains(entry.getKey()))
                    {
                        // cancel the task
                        entry.getValue().cancel();
                        // remove from our registered list
                        i.remove();
                        logger.info("Stopping sampling of disk IO for device: " + entry.getKey());
                    }
                }
                // clean up the timer
                this.timer.purge();
            }
            catch (SigarException e)
            {
                logger.error("Failed to setup block device sampling", e);
            }
        }
    }
    
    private class DiskIO extends TimerTask
    {
        private final String device;
        
        private final long interval;
        
        private final SampleRingBuffer readBytes;
        
        private final SampleRingBuffer writeBytes;
        
        private final SampleRingBuffer reads;
        
        private final SampleRingBuffer writes;
        
        private int failCount = 0;
        
        public DiskIO(String device, int capacity, long interval, TimeUnit intervalUnit)
        {
            this.device     = device;
            this.interval   = intervalUnit.toMillis(interval);
            this.readBytes  = new SampleRingBuffer(capacity, interval, intervalUnit);
            this.writeBytes = new SampleRingBuffer(capacity, interval, intervalUnit);
            this.reads  = new SampleRingBuffer(capacity, interval, intervalUnit);
            this.writes = new SampleRingBuffer(capacity, interval, intervalUnit);
        }
        
        public void addSample(DiskUsage sample)
        {
            synchronized (this)
            {
                this.readBytes.addSample(sample.getReadBytes());
                this.writeBytes.addSample(sample.getWriteBytes());
                this.reads.addSample(sample.getReads());
                this.writes.addSample(sample.getWrites());
            }
        }
        
        public DiskIORateInfo instantRate()
        {
            synchronized (this)
            {
                return new DiskIORateInfo(
                        this.readBytes.instantRateSeconds(),
                        this.writeBytes.instantRateSeconds(),
                        this.readBytes.peakRateSeconds(),
                        this.writeBytes.peakRateSeconds(),
                        this.reads.instantRateSeconds(),
                        this.writes.instantRateSeconds(),
                        this.reads.peakRateSeconds(),
                        this.writes.peakRateSeconds()
                );
            }
        }
        
        public DiskIORateInfo fiveMinuteRate()
        {
            synchronized (this)
            {
                return new DiskIORateInfo(
                        this.readBytes.averageRateSeconds(),
                        this.writeBytes.averageRateSeconds(),
                        this.readBytes.peakRateSeconds(),
                        this.writeBytes.peakRateSeconds(),
                        this.reads.averageRateSeconds(),
                        this.writes.averageRateSeconds(),
                        this.reads.peakRateSeconds(),
                        this.writes.peakRateSeconds()
                );
            }
        }

        @Override
        public void run()
        {
            // sample this disk
            try
            {
                this.addSample(sigar.getDiskUsage(this.device));
            }
            catch (SigarException e)
            {
                this.failCount++;
                if (this.failCount > 6)
                {
                    // cancel ourselves
                    this.cancel();
                    registeredDevices.remove(this.device);
                    logger.warn("Error sampling block device", e);
                    logger.info("Stopping sampling of disk IO for device: " + this.device);
                }
            }
        }
    }
    
    public static void main(String[] args)
    {
        new DiskIOHandler();
    }
}
