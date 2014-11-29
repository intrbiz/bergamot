package com.intrbiz.bergamot.model.message.agent.stat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.stat.uptime")
public class UptimeStat extends AgentMessage
{
    @JsonProperty("uptime")
    private double uptime;

    public UptimeStat()
    {
        super();
    }

    public UptimeStat(AgentMessage message)
    {
        super(message);
    }

    public UptimeStat(String id)
    {
        super(id);
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
