package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlType(name = "worker")
@XmlRootElement(name = "worker")
public class WorkerCfg extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    private ClusterCfg cluster;
    
    private int threads = -1;
    
    private String site;

    private String workerPool;

    private List<EngineCfg> engines = new LinkedList<EngineCfg>();
    
    private LoggingCfg logging;

    public WorkerCfg()
    {
        super();
    }
    
    @XmlAttribute(name = "site")
    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
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
    
    public boolean isEngineEnabled(String engineName, boolean defaultValue)
    {
        for (EngineCfg engine : this.engines)
        {
            if (engineName.equalsIgnoreCase(engine.getName()))
                return engine.isEnabled();
        }
        return defaultValue;
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
    
    
    @XmlElementRef(type = ClusterCfg.class)
    public ClusterCfg getCluster()
    {
        return cluster;
    }

    public void setCluster(ClusterCfg cluster)
    {
        this.cluster = cluster;
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
        if (this.cluster == null)
        {
            this.cluster = new ClusterCfg(new String[] { "127.0.0.1:2181" });
        }
        // default number of threads
        if (this.threads <= 0)
        {
            // default of 5 checks per processor
            this.threads = Runtime.getRuntime().availableProcessors() * 5;
        }
        // logging defaults
        if (this.logging == null)
        {
            this.logging = new LoggingCfg();
        }
    }
    
    public static WorkerCfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(WorkerCfg.class, new FileInputStream(file));
    }
}
