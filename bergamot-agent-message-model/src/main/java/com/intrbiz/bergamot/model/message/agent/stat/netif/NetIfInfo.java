package com.intrbiz.bergamot.model.message.agent.stat.netif;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.netif-info")
public class NetIfInfo extends AgentType
{
    @JsonProperty("name")
    private String name;

    @JsonProperty("hw-address")
    private String hwAddress;

    @JsonProperty("address")
    private String address;

    @JsonProperty("netmask")
    private String netmask;

    @JsonProperty("tx-bytes")
    private long txBytes;

    @JsonProperty("rx-bytes")
    private long rxBytes;

    public NetIfInfo()
    {
        super();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getHwAddress()
    {
        return hwAddress;
    }

    public void setHwAddress(String hwAddress)
    {
        this.hwAddress = hwAddress;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getNetmask()
    {
        return netmask;
    }

    public void setNetmask(String netmask)
    {
        this.netmask = netmask;
    }

    public long getTxBytes()
    {
        return txBytes;
    }

    public void setTxBytes(long txBytes)
    {
        this.txBytes = txBytes;
    }

    public long getRxBytes()
    {
        return rxBytes;
    }

    public void setRxBytes(long rxBytes)
    {
        this.rxBytes = rxBytes;
    }
}
