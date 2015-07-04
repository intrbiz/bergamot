package com.intrbiz.bergamot.model.message.agent.ping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.ping")
public class AgentPing extends AgentMessage
{
    @JsonProperty("timestamp")
    private long timestamp;
    
    public AgentPing()
    {
        super();
    }

    public AgentPing(String id, long timestamp)
    {
        super(id);
        this.timestamp = timestamp;
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
