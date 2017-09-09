package com.intrbiz.bergamot.config;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.configuration.Configuration;
import com.intrbiz.util.uuid.UUIDAdapter;

public abstract class NotifierCfg extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    private int threads = -1;

    private BrokerCfg broker;

    private List<NotificationEngineCfg> engines = new LinkedList<NotificationEngineCfg>();

    private UUID site = null;
    
    private int sleepTime = 6;
    
    private LoggingCfg logging;

    public NotifierCfg()
    {
        super();
    }

    @XmlAttribute(name = "site")
    @XmlJavaTypeAdapter(UUIDAdapter.class)
    public UUID getSite()
    {
        return site;
    }

    public void setSite(UUID site)
    {
        this.site = site;
    }

    @XmlElementRef(type = BrokerCfg.class)
    public BrokerCfg getBroker()
    {
        return broker;
    }

    public void setBroker(BrokerCfg broker)
    {
        this.broker = broker;
    }

    @XmlAttribute(name = "threads")
    public int getThreads()
    {
        return threads;
    }

    public void setThreads(int threads)
    {
        this.threads = threads;
    }

    @XmlElementRef(type = NotificationEngineCfg.class)
    public List<NotificationEngineCfg> getEngines()
    {
        return engines;
    }

    public void setEngines(List<NotificationEngineCfg> engines)
    {
        this.engines = engines;
    }

    /**
     * How long (seconds) to sleep after sending a message.  
     * Some mail providers will disabled accounts sending too 
     * fast.  Setting sleep-time > 0 will cause the 
     * notification engine to sleep before sending the 
     * next message.
     * 
     */
    @XmlAttribute(name = "sleep-time")
    public int getSleepTime()
    {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime)
    {
        this.sleepTime = sleepTime;
    }
    
    @XmlElementRef(type = LoggingCfg.class)
    public LoggingCfg getLogging()
    {
        return logging;
    }

    public void setLogging(LoggingCfg logging)
    {
        this.logging = logging;
    }

    @Override
    public void applyDefaults()
    {
        // the broker
        if (this.broker == null)
        {
            this.broker = new BrokerCfg("hcq", new String[] { "ws://127.0.0.1:1543/hcq", "ws://127.0.0.1:1544/hcq", "ws://127.0.0.1:1545/hcq" });
        }
        // default number of threads
        if (this.getThreads() <= 0)
        {
            this.setThreads(Runtime.getRuntime().availableProcessors());
        }
        // logging defaults
        if (this.logging == null)
        {
            this.logging = new LoggingCfg();
        }
    }
}
