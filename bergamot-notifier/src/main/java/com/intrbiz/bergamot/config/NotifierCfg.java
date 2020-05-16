package com.intrbiz.bergamot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.configuration.Configuration;

@XmlType(name = "notifier")
@XmlRootElement(name = "notifier")
public class NotifierCfg extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    private ClusterCfg cluster;
    
    private String threads;
    
    private String site;
    
    private LoggingCfg logging;

    public NotifierCfg()
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

    @XmlAttribute(name = "threads")
    public String getThreads()
    {
        return threads;
    }

    public void setThreads(String threads)
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
        // logging defaults
        if (this.logging == null)
        {
            this.logging = new LoggingCfg();
        }
    }
    
    public static NotifierCfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(NotifierCfg.class, new FileInputStream(file));
    }
}
