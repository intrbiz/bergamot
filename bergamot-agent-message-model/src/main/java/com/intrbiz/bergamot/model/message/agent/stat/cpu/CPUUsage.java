package com.intrbiz.bergamot.model.message.agent.stat.cpu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.agent.model.cpu-usage")
public class CPUUsage extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("total")
    private double total;

    @JsonProperty("system")
    private double system;

    @JsonProperty("user")
    private double user;

    @JsonProperty("wait")
    private double wait;

    public CPUUsage()
    {
        super();
    }

    public double getTotal()
    {
        return total;
    }

    public void setTotal(double total)
    {
        this.total = total;
    }

    public double getSystem()
    {
        return system;
    }

    public void setSystem(double system)
    {
        this.system = system;
    }

    public double getUser()
    {
        return user;
    }

    public void setUser(double user)
    {
        this.user = user;
    }

    public double getWait()
    {
        return wait;
    }

    public void setWait(double wait)
    {
        this.wait = wait;
    }
}
