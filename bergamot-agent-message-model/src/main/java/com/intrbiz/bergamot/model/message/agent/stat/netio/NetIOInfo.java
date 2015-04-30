package com.intrbiz.bergamot.model.message.agent.stat.netio;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.netio-info")
public class NetIOInfo extends AgentType
{
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("instant-rate")
    private NetIORateInfo instantRate;
    
    @JsonProperty("five-minute-rate")
    private NetIORateInfo fiveMinuteRate;

    public NetIOInfo()
    {
        super();
    }
    
    public NetIOInfo(String name)
    {
        super();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public NetIORateInfo getInstantRate()
    {
        return instantRate;
    }

    public void setInstantRate(NetIORateInfo instantRate)
    {
        this.instantRate = instantRate;
    }

    public NetIORateInfo getFiveMinuteRate()
    {
        return fiveMinuteRate;
    }

    public void setFiveMinuteRate(NetIORateInfo fiveMinuteRate)
    {
        this.fiveMinuteRate = fiveMinuteRate;
    }
}
