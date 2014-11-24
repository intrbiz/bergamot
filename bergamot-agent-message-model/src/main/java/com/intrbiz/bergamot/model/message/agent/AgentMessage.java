package com.intrbiz.bergamot.model.message.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.intrbiz.bergamot.io.BergamotAgentTranscoder;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class AgentMessage
{
    @JsonProperty("id")
    protected String id;
    
    public AgentMessage()
    {
        super();
    }
    
    public AgentMessage(String id)
    {
        super();
        this.id = id;
    }
    
    public AgentMessage(AgentMessage message)
    {
        super();
        this.id = message.getId();
    }

    public final String getId()
    {
        return id;
    }

    public final void setId(String id)
    {
        this.id = id;
    }
    
    public String toString()
    {
        return BergamotAgentTranscoder.getDefaultInstance().encodeAsString(this);
    }
}
