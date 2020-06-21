package com.intrbiz.bergamot.model.message.agent.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.stat.uptime")
public class UptimeStat extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("uptime")
    private double uptime;

    public UptimeStat()
    {
        super();
    }

    public UptimeStat(Message message)
    {
        super(message);
    }

    public double getUptime()
    {
        return uptime;
    }

    public void setUptime(double uptime)
    {
        this.uptime = uptime;
    }
}
