package com.intrbiz.bergamot.model.message.agent.stat.cpu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentType;

@JsonTypeName("bergamot.agent.model.cpu-time")
public class CPUTime extends AgentType
{
    @JsonProperty("total")
    private long total;
    
    @JsonProperty("system")
    private long system;
    
    @JsonProperty("user")
    private long user;
    
    @JsonProperty("wait")
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
