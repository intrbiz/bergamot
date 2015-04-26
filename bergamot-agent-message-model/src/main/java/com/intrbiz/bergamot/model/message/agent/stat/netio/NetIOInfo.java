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
    
    @JsonProperty("one-minute-rate")
    private NetIORateInfo oneMinuteRate;
    
    @JsonProperty("five-minute-rate")
    private NetIORateInfo fiveMinuteRate;
    
    @JsonProperty("fifteen-minute-rate")
    private NetIORateInfo fifteenMinuteRate;

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

    public NetIORateInfo getOneMinuteRate()
    {
        return oneMinuteRate;
    }

    public void setOneMinuteRate(NetIORateInfo oneMinuteRate)
    {
        this.oneMinuteRate = oneMinuteRate;
    }

    public NetIORateInfo getFiveMinuteRate()
    {
        return fiveMinuteRate;
    }

    public void setFiveMinuteRate(NetIORateInfo fiveMinuteRate)
    {
        this.fiveMinuteRate = fiveMinuteRate;
    }

    public NetIORateInfo getFifteenMinuteRate()
    {
        return fifteenMinuteRate;
    }

    public void setFifteenMinuteRate(NetIORateInfo fifteenMinuteRate)
    {
        this.fifteenMinuteRate = fifteenMinuteRate;
    }
}
