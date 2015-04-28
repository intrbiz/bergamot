package com.intrbiz.bergamot.agent.handler;

import java.security.SecureRandom;
import java.util.ArrayList;
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

import com.intrbiz.bergamot.agent.AgentHandler;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckDiskIO;
import com.intrbiz.bergamot.model.message.agent.stat.DiskIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIOInfo;
import com.intrbiz.bergamot.model.message.agent.stat.diskio.DiskIORateInfo;

public class DiskIOHandler implements AgentHandler
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
            if (disk != null && disk.canComputeRates())
            {
                DiskIOInfo info = new DiskIOInfo(name);
                info.setInstantRate(disk.computeInstantRate().toInfo());
                info.setOneMinuteRate(disk.computeRate(1, TimeUnit.MINUTES).toInfo());
                info.setFiveMinuteRate(disk.computeRate(5, TimeUnit.MINUTES).toInfo());
                info.setFifteenMinuteRate(disk.computeRate(15, TimeUnit.MINUTES).toInfo());
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
                        if (! this.registeredDevices.containsKey(device))
                        {
                            // setup
                            DiskIO diskIO = new DiskIO(device.getDevName());
                            // register
                            this.registeredDevices.put(device.getDevName(), diskIO);
                            // schedule
                            long skew = ((long) (diskIO.sampleInterval * random.nextDouble()));
                            this.timer.scheduleAtFixedRate(diskIO, skew, diskIO.sampleInterval);
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
    
    protected static double round(double d)
    {
        return (((double) Math.round(d * 1000)) / 1000D);
    }
    
    private class DiskIO extends TimerTask
    {
        private final String device;
        
        private final long sampleInterval;
        
        private final long sampleSize;
        
        private final ArrayList<DiskIOSample> samples;
        
        public DiskIO(String device, long sampleInterval, TimeUnit sampleIntervalUnit, long sampleSize, TimeUnit sampleSizeUnit)
        {
            this.device = device;
            this.sampleInterval = sampleIntervalUnit.toMillis(sampleInterval);
            this.sampleSize = sampleSizeUnit.toMillis(sampleSize) / this.sampleInterval;
            this.samples = new ArrayList<DiskIOSample>((int) this.sampleSize + 1);
        }
        
        public DiskIO(String device)
        {
            this(device, 5, TimeUnit.SECONDS, 1, TimeUnit.HOURS);
        }
        
        public void addSample(DiskIOSample sample)
        {
            synchronized (this)
            {
                // ensure we stay under the sample size
                while (this.samples.size() >= this.sampleSize)
                {
                    this.samples.remove(0);
                }
                this.samples.add(sample);
            }
        }
        
        public boolean canComputeRates()
        {
            return this.samples.size() > 1;
        }
        
        /**
         * Compute the rate of the last two samples
         */
        public DiskIORate computeInstantRate()
        {
            synchronized (this)
            {
                if (this.samples.size() < 2) return null;
                // compute the rate of change between the last two samples
                DiskIOSample previous = this.samples.get(this.samples.size() - 2);
                DiskIOSample current  = this.samples.get(this.samples.size() - 1);
                // compute the rate
                return current.computeRate(previous);
            }
        }
        
        /**
         * Compute the throughput over a specific time scale (data permitting)
         */        
        public DiskIORate computeRate(long interval, TimeUnit unit)
        {
            synchronized (this)
            {
                if (this.samples.size() < 2) return null;
                // indexes
                int oldestIndex = Math.max((this.samples.size() - 1) - (int)(unit.toMillis(interval) / this.sampleInterval), 0);
                int latestIndex = this.samples.size() - 1;
                // compute the average rate
                DiskIOSample latest = this.samples.get(latestIndex);
                DiskIOSample oldest = this.samples.get(oldestIndex);
                DiskIORate rate = latest.computeRate(oldest);
                // compute the peak rate
                for (int i = latestIndex; i > oldestIndex; i--)
                {
                    oldest = this.samples.get(i -1);
                    latest = this.samples.get(i);
                    DiskIORate peak = latest.computeRate(oldest);
                    rate.readPeakRate  = Math.max(rate.readPeakRate,  peak.readRate);
                    rate.writePeakRate = Math.max(rate.writePeakRate, peak.writeRate);
                    rate.peakReads     = Math.max(rate.peakReads,     peak.reads);
                    rate.peakWrites    = Math.max(rate.peakWrites,    peak.writes);
                }
                return rate;
            }
        }

        @Override
        public void run()
        {
            // sample this interface
            try
            {
                 DiskUsage usage = sigar.getDiskUsage(this.device);
                 if (usage != null)
                 {
                     this.addSample(
                             new DiskIOSample(
                                     System.currentTimeMillis(), 
                                     usage.getReadBytes(), 
                                     usage.getWriteBytes(),
                                     usage.getReads(),
                                     usage.getWrites(),
                                     usage.getQueue(),
                                     usage.getServiceTime()
                             )
                     );
                 }
            }
            catch (SigarException e)
            {
                logger.warn("Error sampling block device", e);
            }
        }
    }
    
    public static class DiskIOSample
    {
        public final long sampledAt;
        
        public final long readBytes;
        
        public final long writeBytes;
        
        public final long reads;
        
        public final long writes;
        
        public final double queue;
        
        public final double serviceTime;
        
        public DiskIOSample(long sampledAt, long readBytes, long writeBytes, long reads, long writes, double queue, double serviceTime)
        {
            this.sampledAt = sampledAt;
            this.readBytes = readBytes;
            this.writeBytes = writeBytes;
            this.reads = reads;
            this.writes = writes;
            this.queue = queue;
            this.serviceTime = serviceTime;
        }
        
        public DiskIORate computeRate(DiskIOSample previous)
        {
            // compute the rate
            double interval = (double) ((this.sampledAt - previous.sampledAt) / 1000L);
            double readDelta  = (double) (this.readBytes   - previous.readBytes);
            double writeDelta  = (double) (this.writeBytes   - previous.writeBytes);
            double readsDelta  = (double) (this.reads   - previous.reads);
            double writesDelta  = (double) (this.writes   - previous.writes);
            // validate
            if (interval < 0 || readDelta < 0 || writeDelta < 0) return null;
            // return
            return new DiskIORate( readDelta / interval, writeDelta / interval, readsDelta / interval, writesDelta / interval);
        }
    }
    
    public static class DiskIORate
    {
        /** Rate is B/s */
        public final double readRate;
        
        /** Rate is B/s */
        public final double writeRate;
        
        /** Reads per second */
        public final double reads;
        
        /** Writes per second */
        public final double writes;
        
        /** Peak Rate is B/s */
        public double readPeakRate;
        
        /** Peak Rate is B/s */
        public double writePeakRate;
        
        /** Peak Reads per second */
        public double peakReads;
        
        /** Peak Writes per second */
        public double peakWrites;
        
        public DiskIORate(double readRate, double writeRate, double reads, double writes)
        {
            this.readRate = readRate;
            this.writeRate = writeRate;
            this.readPeakRate = readRate;
            this.writePeakRate = writeRate;
            this.reads = reads;
            this.writes = writes;
            this.peakReads = reads;
            this.peakWrites = writes;
        }
        
        // Read
        
        public double getReadRateMBps()
        {
            return this.readRate / 1000000D;
        }
        
        public double getReadRatekBps()
        {
            return this.readRate / 1000D;
        }
        
        public double getReadRateBps()
        {
            return this.readRate;
        }
        
        // Write
        
        public double getWriteRateMBps()
        {
            return this.writeRate / 1000000D;
        }
        
        public double getWriteRatekBps()
        {
            return this.writeRate / 1000D;
        }
        
        public double getWriteRateBps()
        {
            return this.writeRate;
        }
        
        // Peak Read
        
        public double getReadPeakRateMBps()
        {
            return this.readPeakRate / 1000000D;
        }
        
        public double getReadPeakRatekBps()
        {
            return this.readPeakRate / 1000D;
        }
        
        public double getReadPeakRateBps()
        {
            return this.readPeakRate;
        }
        
        // Peak Write
        
        public double getWritePeakRateMBps()
        {
            return this.writePeakRate / 1000000D;
        }
        
        public double getWritePeakRatekBps()
        {
            return this.writePeakRate / 1000D;
        }
        
        public double getWritePeakRateBps()
        {
            return this.writePeakRate;
        }
        
        // Util
        
        public DiskIORateInfo toInfo()
        {
            return new DiskIORateInfo(this.readRate, this.writeRate, this.readPeakRate, this.writePeakRate, this.reads, this.writes, this.peakReads, this.peakWrites);
        }
        
        public String toString()
        {
            return "Read: " + this.reads + "/s " + this.getReadRateMBps() + "(" + this.getReadPeakRateMBps() + ")Mb/s, Write: " + this.writes + "/s " + this.getWriteRateMBps() + "(" + this.getWritePeakRateMBps() + ")Mb/s";
        }
    }
    
    public static void main(String[] args)
    {
        new DiskIOHandler();
    }
}
