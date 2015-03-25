package com.intrbiz.bergamot.model.message.agent.stat;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.stat.netif.NetIfInfo;
import com.intrbiz.bergamot.model.message.agent.stat.netif.NetRouteInfo;

@JsonTypeName("bergamot.agent.stat.netif")
public class NetIfStat extends AgentMessage
{
    @JsonProperty("hostname")
    private String hostname;
    
    @JsonProperty("routes")
    private List<NetRouteInfo> routes = new LinkedList<NetRouteInfo>();
    
    @JsonProperty("interfaces")
    private List<NetIfInfo> ifaces = new LinkedList<NetIfInfo>();

    public NetIfStat()
    {
        super();
    }

    public NetIfStat(AgentMessage message)
    {
        super(message);
    }

    public NetIfStat(String id)
    {
        super(id);
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }

    public List<NetRouteInfo> getRoutes()
    {
        return routes;
    }

    public void setRoutes(List<NetRouteInfo> routes)
    {
        this.routes = routes;
    }

    public List<NetIfInfo> getIfaces()
    {
        return ifaces;
    }

    public void setIfaces(List<NetIfInfo> ifaces)
    {
        this.ifaces = ifaces;
    }
}
