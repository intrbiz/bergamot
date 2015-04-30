package com.intrbiz.bergamot.agent.handler;

import java.security.SecureRandom;
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
import com.intrbiz.bergamot.agent.util.SampleRingBuffer;
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
            if (net != null)
            {
                NetIOInfo info = new NetIOInfo(name);
                info.setInstantRate(net.instantRate());
                info.setFiveMinuteRate(net.fiveMinuteRate());
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
                        NetIO netIO = new NetIO(interfaceName, 60, 5L, TimeUnit.SECONDS);
                        // register
                        this.registeredInterfaces.put(interfaceName, netIO);
                        // schedule
                        long skew = ((long) (netIO.interval * random.nextDouble()));
                        this.timer.scheduleAtFixedRate(netIO, skew, netIO.interval);
                        logger.info("Starting sampling of network IO for interface: " + interfaceName + " every 5 seconds");
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
                        logger.info("Stopping sampling of network IO for interface: " + entry.getKey());
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
        
        private final long interval;
        
        private final SampleRingBuffer txBytes;
        
        private final SampleRingBuffer rxBytes;
        
        private int failCount = 0;
        
        public NetIO(String interfaceName, int capacity, long interval, TimeUnit intervalUnit)
        {
            this.interfaceName = interfaceName;
            this.interval = intervalUnit.toMillis(interval);
            this.txBytes = new SampleRingBuffer(capacity, interval, intervalUnit);
            this.rxBytes = new SampleRingBuffer(capacity, interval, intervalUnit);
        }
        
        public void addSample(NetInterfaceStat sample)
        {
            synchronized (this)
            {
                this.txBytes.addSample(sample.getTxBytes());
                this.rxBytes.addSample(sample.getRxBytes());
            }
        }
        
        public NetIORateInfo instantRate()
        {
            synchronized (this)
            {
                return new NetIORateInfo(
                        this.txBytes.instantRateSeconds(),
                        this.rxBytes.instantRateSeconds(),
                        this.txBytes.peakRateSeconds(),
                        this.rxBytes.peakRateSeconds()
                );
            }
        }
        
        public NetIORateInfo fiveMinuteRate()
        {
            synchronized (this)
            {
                return new NetIORateInfo(
                        this.txBytes.averageRateSeconds(),
                        this.rxBytes.averageRateSeconds(),
                        this.txBytes.peakRateSeconds(),
                        this.rxBytes.peakRateSeconds()
                );
            }
        }

        @Override
        public void run()
        {
            // sample this interface
            try
            {
                this.addSample(sigar.getNetInterfaceStat(this.interfaceName));
            }
            catch (SigarException e)
            {
                this.failCount++;
                if (this.failCount > 6)
                {
                    // cancel ourselves
                    this.cancel();
                    registeredInterfaces.remove(this.interfaceName);
                    logger.warn("Error sampling network interface", e);
                    logger.info("Stopping sampling of network IO for interface: " + this.interfaceName);
                }
            }
        }
    }    
}
