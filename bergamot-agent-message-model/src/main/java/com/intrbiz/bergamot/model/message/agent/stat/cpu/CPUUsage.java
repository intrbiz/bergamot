package com.intrbiz.bergamot.model.message.agent.stat.cpu;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.cpu-usage")
public class CPUUsage extends AgentType
{
    private double total;

    private double system;

    private double user;

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
