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
    private int threads = -1;

    private BrokerCfg broker;

    private List<NotificationEngineCfg> engines = new LinkedList<NotificationEngineCfg>();

    private UUID site = null;

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

    @Override
    public void applyDefaults()
    {
        // the broker
        if (this.broker == null)
        {
            this.broker = new BrokerCfg("amqp://127.0.0.1");
        }
        // default number of threads
        if (this.getThreads() <= 0)
        {
            this.setThreads(Runtime.getRuntime().availableProcessors());
        }
    }
}
