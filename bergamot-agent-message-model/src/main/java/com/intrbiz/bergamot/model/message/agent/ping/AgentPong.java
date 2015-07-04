package com.intrbiz.bergamot.model.message.agent.ping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.pong")
public class AgentPong extends AgentMessage
{
    @JsonProperty("timestamp")
    private long timestamp;
    
    public AgentPong()
    {
        super();
    }

    public AgentPong(AgentPing message)
    {
        super(message);
        this.timestamp = message.getTimestamp();
    }
    
    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }
}
