package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.configuration.Configuration;
import com.intrbiz.util.uuid.UUIDAdapter;

public abstract class WatcherCfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private BrokerCfg broker;
    
    private UUID site;

    private UUID location;

    private List<EngineCfg> engines = new LinkedList<EngineCfg>();

    public WatcherCfg()
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

    @XmlAttribute(name = "location")
    public UUID getLocation()
    {
        return location;
    }

    public void setLocation(UUID location)
    {
        this.location = location;
    }

    public WatcherCfg(EngineCfg... engines)
    {
        super();
        for (EngineCfg engine : engines)
        {
            this.engines.add(engine);
        }
    }

    @XmlElementRef(type=EngineCfg.class)
    public List<EngineCfg> getEngines()
    {
        return engines;
    }

    public void setEngines(List<EngineCfg> engines)
    {
        this.engines = engines;
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

    @Override
    public void applyDefaults()
    {
        // the broker
        if (this.broker == null)
        {
            this.broker = new BrokerCfg("amqp://127.0.0.1");
        }
        // cascade
        for (EngineCfg engine : this.engines)
        {
            engine.applyDefaults();
        }
    }
    
    public static WatcherCfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(WatcherCfg.class, new FileInputStream(file));
    }
}
