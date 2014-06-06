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

public abstract class WorkerCfg extends Configuration
{
    private BrokerCfg broker;
    
    private int threads = -1;
    
    private UUID site;

    private String workerPool;

    private List<EngineCfg> engines = new LinkedList<EngineCfg>();

    public WorkerCfg()
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

    @XmlAttribute(name = "worker-pool")
    public String getWorkerPool()
    {
        return workerPool;
    }

    public void setWorkerPool(String workerPool)
    {
        this.workerPool = workerPool;
    }

    public WorkerCfg(EngineCfg... engines)
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

    @XmlAttribute(name = "threads")
    public int getThreads()
    {
        return threads;
    }

    public void setThreads(int threads)
    {
        this.threads = threads;
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
        // default number of threads
        if (this.threads <= 0)
        {
            // default of 5 checks per processor
            this.threads = Runtime.getRuntime().availableProcessors() * 5;
        }
        // cascade
        for (EngineCfg engine : this.engines)
        {
            engine.applyDefaults();
        }
    }
    
    public static WorkerCfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(WorkerCfg.class, new FileInputStream(file));
    }
}
