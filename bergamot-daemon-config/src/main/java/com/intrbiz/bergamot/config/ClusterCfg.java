package com.intrbiz.bergamot.config;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "cluster")
@XmlRootElement(name = "cluster")
public class ClusterCfg
{    
    private List<String> zooKeeper = new LinkedList<String>();
    
    private String publicAddress;
    
    private int port;
    
    private int expectedMembers = 0;
    
    private String proxyUrl;
    
    private String proxyKey;

    public ClusterCfg()
    {
        super();
    }
    
    public ClusterCfg(String[] zooKeeper)
    {
        super();
        this.zooKeeper.addAll(Arrays.asList(zooKeeper));
    }

    @XmlElement(name = "zookeeper")
    public List<String> getZooKeeper()
    {
        return this.zooKeeper;
    }

    public void setZooKeeper(List<String> zookeeper)
    {
        this.zooKeeper = zookeeper;
    }

    @XmlAttribute(name = "public-address")
    public String getPublicAddress()
    {
        return this.publicAddress;
    }

    public void setPublicAddress(String publicAddress)
    {
        this.publicAddress = publicAddress;
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

    @XmlAttribute(name = "expected-members")
    public int getExpectedMembers()
    {
        return this.expectedMembers;
    }

    public void setExpectedMembers(int expectedMembers)
    {
        this.expectedMembers = expectedMembers;
    }

    @XmlAttribute(name = "proxy-url")
    public String getProxyUrl()
    {
        return this.proxyUrl;
    }

    public void setProxyUrl(String proxyUrl)
    {
        this.proxyUrl = proxyUrl;
    }

    @XmlAttribute(name = "proxy-key")
    public String getProxyKey()
    {
        return this.proxyKey;
    }

    public void setProxyKey(String proxyKey)
    {
        this.proxyKey = proxyKey;
    }
}
