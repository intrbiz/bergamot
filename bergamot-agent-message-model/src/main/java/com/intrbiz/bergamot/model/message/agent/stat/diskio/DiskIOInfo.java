package com.intrbiz.bergamot.model.message.agent.stat.diskio;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.diskio-info")
public class DiskIOInfo extends AgentType
{
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("instant-rate")
    private DiskIORateInfo instantRate;
    
    @JsonProperty("one-minute-rate")
    private DiskIORateInfo oneMinuteRate;
    
    @JsonProperty("five-minute-rate")
    private DiskIORateInfo fiveMinuteRate;
    
    @JsonProperty("fifteen-minute-rate")
    private DiskIORateInfo fifteenMinuteRate;

    public DiskIOInfo()
    {
        super();
    }
    
    public DiskIOInfo(String name)
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

    public DiskIORateInfo getInstantRate()
    {
        return instantRate;
    }

    public void setInstantRate(DiskIORateInfo instantRate)
    {
        this.instantRate = instantRate;
    }

    public DiskIORateInfo getOneMinuteRate()
    {
        return oneMinuteRate;
    }

    public void setOneMinuteRate(DiskIORateInfo oneMinuteRate)
    {
        this.oneMinuteRate = oneMinuteRate;
    }

    public DiskIORateInfo getFiveMinuteRate()
    {
        return fiveMinuteRate;
    }

    public void setFiveMinuteRate(DiskIORateInfo fiveMinuteRate)
    {
        this.fiveMinuteRate = fiveMinuteRate;
    }

    public DiskIORateInfo getFifteenMinuteRate()
    {
        return fifteenMinuteRate;
    }

    public void setFifteenMinuteRate(DiskIORateInfo fifteenMinuteRate)
    {
        this.fifteenMinuteRate = fifteenMinuteRate;
    }
}
