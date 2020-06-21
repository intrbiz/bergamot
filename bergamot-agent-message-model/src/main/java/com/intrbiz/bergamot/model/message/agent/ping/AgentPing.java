package com.intrbiz.bergamot.model.message.agent.ping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.ping")
public class AgentPing extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    public AgentPing()
    {
        super();
    }

    public AgentPing(long timestamp)
    {
        super();
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
