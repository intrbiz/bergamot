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

@XmlType(name = "proxy")
@XmlRootElement(name = "proxy")
public class ProxyCfg extends Configuration
{
    private static final long serialVersionUID = 1L;

    private int port;

    private ClusterCfg cluster;

    private LoggingCfg logging;

    public ProxyCfg()
    {
        super();
    }

    @XmlAttribute(name = "port")
    public int getPort()
    {
        return this.port;
    }

    public void setPort(int port)
    {
        this.port = port;
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
        if (this.port <= 0)
            this.port = 14080;
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

    public static ProxyCfg read(File file) throws JAXBException, IOException
    {
        return Configuration.read(ProxyCfg.class, new FileInputStream(file));
    }
}
