package com.intrbiz.bergamot.model.message.agent.stat.cpu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.cpu-info")
public class CPUInfo extends AgentType
{
    @JsonProperty("vendor")
    private String vendor;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("speed")
    private int speed;
    
    public CPUInfo()
    {
        super();
    }

    public String getVendor()
    {
        return vendor;
    }

    public void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public int getSpeed()
    {
        return speed;
    }

    public void setSpeed(int speed)
    {
        this.speed = speed;
    }
}
