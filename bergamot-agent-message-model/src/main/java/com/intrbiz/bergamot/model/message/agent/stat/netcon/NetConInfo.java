package com.intrbiz.bergamot.model.message.agent.stat.netcon;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.netcon-info")
public class NetConInfo extends AgentType
{
    @JsonProperty("protocol")
    private String protocol;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("local_address")
    private String localAddress;
    
    @JsonProperty("local_port")
    private int localPort;
    
    @JsonProperty("remote_address")
    private String remoteAddress;
    
    @JsonProperty("remote_port")
    private int remotePort;
    
    public NetConInfo()
    {
        super();
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getLocalAddress()
    {
        return localAddress;
    }

    public void setLocalAddress(String localAddress)
    {
        this.localAddress = localAddress;
    }

    public int getLocalPort()
    {
        return localPort;
    }

    public void setLocalPort(int localPort)
    {
        this.localPort = localPort;
    }

    public String getRemoteAddress()
    {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress)
    {
        this.remoteAddress = remoteAddress;
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    public void setRemotePort(int remotePort)
    {
        this.remotePort = remotePort;
    }
}
