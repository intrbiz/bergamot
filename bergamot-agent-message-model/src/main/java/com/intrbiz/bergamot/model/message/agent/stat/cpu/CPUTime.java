package com.intrbiz.bergamot.model.message.agent.stat.cpu;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.cpu-time")
public class CPUTime extends AgentType
{
    private long total;
    
    private long system;
    
    private long user;
    
    private long wait;
    
    public CPUTime()
    {
        super();
    }

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public long getSystem()
    {
        return system;
    }

    public void setSystem(long system)
    {
        this.system = system;
    }

    public long getUser()
    {
        return user;
    }

    public void setUser(long user)
    {
        this.user = user;
    }

    public long getWait()
    {
        return wait;
    }

    public void setWait(long wait)
    {
        this.wait = wait;
    }
}
