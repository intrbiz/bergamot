package com.intrbiz.bergamot.model.message.agent.stat.netif;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.netroute-info")
public class NetRouteInfo extends AgentType
{
    @JsonProperty("destination")
    private String destination;
    
    @JsonProperty("mask")
    private String mask;
    
    @JsonProperty("gateway")
    private String gateway;
    
    @JsonProperty("metric")
    private long metric;
    
    @JsonProperty("mtu")
    private long mtu;
    
    @JsonProperty("interface")
    private String iface;

    public NetRouteInfo()
    {
        super();
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public String getMask()
    {
        return mask;
    }

    public void setMask(String mask)
    {
        this.mask = mask;
    }

    public String getGateway()
    {
        return gateway;
    }

    public void setGateway(String gateway)
    {
        this.gateway = gateway;
    }

    public long getMetric()
    {
        return metric;
    }

    public void setMetric(long metric)
    {
        this.metric = metric;
    }

    public long getMtu()
    {
        return mtu;
    }

    public void setMtu(long mtu)
    {
        this.mtu = mtu;
    }

    public String getInterface()
    {
        return iface;
    }

    public void setInterface(String iface)
    {
        this.iface = iface;
    }
}
