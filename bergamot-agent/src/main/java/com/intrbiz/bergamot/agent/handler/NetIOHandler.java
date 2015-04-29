package com.intrbiz.bergamot.agent.handler;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

import com.intrbiz.bergamot.agent.AgentHandler;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.check.CheckNetIO;
import com.intrbiz.bergamot.model.message.agent.stat.NetIOStat;
import com.intrbiz.bergamot.model.message.agent.stat.netio.NetIOInfo;
import com.intrbiz.bergamot.model.message.agent.stat.netio.NetIORateInfo;

public class NetIOHandler implements AgentHandler
{
    private Logger logger = Logger.getLogger(NetIOHandler.class);
    
    private final SigarProxy sigar = Humidor.getInstance().getSigar();
    
    private final Timer timer = new Timer();
    
    private final Map<String, NetIO> registeredInterfaces = new ConcurrentHashMap<String, NetIO>();

    public NetIOHandler()
    {
        super();
        // register polling of known interfaces
        this.setupInterfaceSampling();
        // periodically setup interfaces
        this.timer.scheduleAtFixedRate(new TimerTask()
        {
            public void run()
            {
                setupInterfaceSampling();
            }
        }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1));
    }
    
    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] {
                CheckNetIO.class
        };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        CheckNetIO check = (CheckNetIO) request;
        NetIOStat stat = new NetIOStat(request);
        // get stats for our interfaces
        for (String name : (check.getInterfaces() == null || check.getInterfaces().isEmpty() ? this.registeredInterfaces.keySet() : check.getInterfaces()))
        {
            NetIO net = this.registeredInterfaces.get(name);
            if (net != null && net.canComputeRates())
            {
                NetIOInfo info = new NetIOInfo(name);
                info.setInstantRate(net.computeInstantRate().toInfo());
                info.setOneMinuteRate(net.computeRate(1, TimeUnit.MINUTES).toInfo());
                info.setFiveMinuteRate(net.computeRate(5, TimeUnit.MINUTES).toInfo());
                info.setFifteenMinuteRate(net.computeRate(15, TimeUnit.MINUTES).toInfo());
                stat.getIfaces().add(info);
            }
        }
        return stat;
    }
    
    protected void setupInterfaceSampling()
    {
        synchronized (this)
        {
            try
            {
                Set<String> interfaces = new TreeSet<String>(Arrays.asList(this.sigar.getNetInterfaceList()));
                // add interfaces not yet setup
                SecureRandom random = new SecureRandom();
                for (String interfaceName : interfaces)
                {
                    if (! this.registeredInterfaces.containsKey(interfaceName))
                    {
                        // setup
                        NetIO netIO = new NetIO(interfaceName);
                        // register
                        this.registeredInterfaces.put(interfaceName, netIO);
                        // schedule
                        long skew = ((long) (netIO.sampleInterval * random.nextDouble()));
                        this.timer.scheduleAtFixedRate(netIO, skew, netIO.sampleInterval);
                    }
                }
                // remove any interfaces which have gone away
                for (Iterator<Entry<String, NetIO>> i = this.registeredInterfaces.entrySet().iterator(); i.hasNext(); )
                {
                    Entry<String, NetIO> entry = i.next();
                    // remove ?
                    if (! interfaces.contains(entry.getKey()))
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
                logger.error("Failed to setup interface sampling", e);
            }
        }
    }
    
    protected static double round(double d)
    {
        return (((double) Math.round(d * 1000)) / 1000D);
    }
    
    private class NetIO extends TimerTask
    {
        private final String interfaceName;
        
        private final long sampleInterval;
        
        private final long sampleSize;
        
        private final ArrayList<NetIOSample> samples;
        
        public NetIO(String interfaceName, long sampleInterval, TimeUnit sampleIntervalUnit, long sampleSize, TimeUnit sampleSizeUnit)
        {
            this.interfaceName = interfaceName;
            this.sampleInterval = sampleIntervalUnit.toMillis(sampleInterval);
            this.sampleSize = sampleSizeUnit.toMillis(sampleSize) / this.sampleInterval;
            this.samples = new ArrayList<NetIOSample>((int) this.sampleSize + 1);
        }
        
        public NetIO(String interfaceName)
        {
            this(interfaceName, 10, TimeUnit.SECONDS, 15, TimeUnit.MINUTES);
        }
        
        public void addSample(NetIOSample sample)
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
            synchronized (this)
            {
                return this.samples.size() > 1;
            }
        }
        
        /**
         * Compute the rate of the last two samples
         */
        public NetIORate computeInstantRate()
        {
            synchronized (this)
            {
                if (this.samples.size() < 2) return null;
                // compute the rate of change between the last two samples
                NetIOSample previous = this.samples.get(this.samples.size() - 2);
                NetIOSample current  = this.samples.get(this.samples.size() - 1);
                // compute the rate
                return current.computeRate(previous);
            }
        }
        
        /**
         * Compute the throughput over a specific time scale (data permitting)
         */        
        public NetIORate computeRate(long interval, TimeUnit unit)
        {
            synchronized (this)
            {
                if (this.samples.size() < 2) return null;
                // indexes
                int oldestIndex = Math.max((this.samples.size() - 1) - (int)(unit.toMillis(interval) / this.sampleInterval), 0);
                int latestIndex = this.samples.size() - 1;
                // compute the average rate
                NetIOSample latest = this.samples.get(latestIndex);
                NetIOSample oldest = this.samples.get(oldestIndex);
                NetIORate rate = latest.computeRate(oldest);
                // compute the peak rate
                for (int i = latestIndex; i > oldestIndex; i--)
                {
                    oldest = this.samples.get(i -1);
                    latest = this.samples.get(i);
                    NetIORate peak = latest.computeRate(oldest);
                    rate.txPeakRate = Math.max(rate.txPeakRate, peak.txRate);
                    rate.rxPeakRate = Math.max(rate.rxPeakRate, peak.rxRate);
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
                NetInterfaceStat nis = sigar.getNetInterfaceStat(this.interfaceName);
                this.addSample(new NetIOSample(System.currentTimeMillis(), nis.getTxBytes(), nis.getRxBytes()));
            }
            catch (SigarException e)
            {
                logger.warn("Error sampling network interface", e);
            }
        }
    }
    
    public static class NetIOSample
    {
        public final long sampledAt;
        
        public final long txBytes;
        
        public final long rxBytes;
        
        public NetIOSample(long sampledAt, long txBytes, long rxBytes)
        {
            this.sampledAt = sampledAt;
            this.txBytes = txBytes;
            this.rxBytes = rxBytes;
        }
        
        public NetIORate computeRate(NetIOSample previous)
        {
            // compute the rate
            double interval = (double) ((this.sampledAt - previous.sampledAt) / 1000L);
            double txDelta  = (double) (this.txBytes   - previous.txBytes);
            double rxDelta  = (double) (this.rxBytes   - previous.rxBytes);
            // validate
            if (interval < 0 || txDelta < 0 || rxDelta < 0) return null;
            // return
            return new NetIORate( txDelta / interval, rxDelta / interval);
        }
    }
    
    public static class NetIORate
    {
        /** Rate is B/s */
        public final double txRate;
        
        /** Rate is B/s */
        public final double rxRate;
        
        /** Rate is B/s */
        public double txPeakRate;
        
        /** Rate is B/s */
        public double rxPeakRate;
        
        public NetIORate(double txRate, double rxRate)
        {
            this.txRate = txRate;
            this.rxRate = rxRate;
            this.txPeakRate = txRate;
            this.rxPeakRate = rxRate;
        }
        
        public static NetIORate average(NetIORate a, NetIORate b)
        {
            return new NetIORate((a.txRate + b.txRate) / 2D, (a.rxRate + b.rxRate) / 2D);
        }
        
        // TX
        
        public double getTxRateMBps()
        {
            return this.txRate / 1000000D;
        }
        
        public double getTxRatekBps()
        {
            return this.txRate / 1000D;
        }
        
        public double getTxRateBps()
        {
            return this.txRate;
        }
        
        public double getTxRateMbps()
        {
            return this.txRate / 100000D;
        }
        
        public double getTxRatekbps()
        {
            return this.txRate / 100D;
        }
        
        public double getTxRatebps()
        {
            return this.txRate * 10D;
        }
        
        // RX
        
        public double getRxRateMBps()
        {
            return this.rxRate / 1000000D;
        }
        
        public double getRxRatekBps()
        {
            return this.rxRate / 1000D;
        }
        
        public double getRxRateBps()
        {
            return this.rxRate;
        }
        
        public double getRxRateMbps()
        {
            return this.rxRate / 100000D;
        }
        
        public double getRxRatekbps()
        {
            return this.rxRate / 100D;
        }
        
        public double getRxRatebps()
        {
            return this.rxRate * 10D;
        }
        
        // Peak
        
        // TX
        
        public double getTxPeakRateMBps()
        {
            return this.txPeakRate / 1000000D;
        }
        
        public double getTxPeakRatekBps()
        {
            return this.txPeakRate / 1000D;
        }
        
        public double getTxPeakRateBps()
        {
            return this.txPeakRate;
        }
        
        public double getTxPeakRateMbps()
        {
            return this.txPeakRate / 100000D;
        }
        
        public double getTxPeakRatekbps()
        {
            return this.txPeakRate / 100D;
        }
        
        public double getTxPeakRatebps()
        {
            return this.txPeakRate * 10D;
        }
        
        // RX
        
        public double getRxPeakRateMBps()
        {
            return this.rxPeakRate / 1000000D;
        }
        
        public double getRxPeakRatekBps()
        {
            return this.rxPeakRate / 1000D;
        }
        
        public double getRxPeakRateBps()
        {
            return this.rxPeakRate;
        }
        
        public double getRxPeakRateMbps()
        {
            return this.rxPeakRate / 100000D;
        }
        
        public double getRxPeakRatekbps()
        {
            return this.rxPeakRate / 100D;
        }
        
        public double getRxPeakRatebps()
        {
            return this.rxPeakRate * 10D;
        }
        
        // Util
        
        public NetIORateInfo toInfo()
        {
            return new NetIORateInfo(this.txRate, this.rxRate, this.txPeakRate, this.rxPeakRate);
        }
        
        public String toString()
        {
            return "Tx: " + this.getTxRateMbps() + "(" + this.getTxPeakRateMbps() + ")Mb/s, Rx: " + this.getRxRateMbps() + "(" + this.getRxPeakRateMbps() + ")Mb/s";
        }
    }
}
