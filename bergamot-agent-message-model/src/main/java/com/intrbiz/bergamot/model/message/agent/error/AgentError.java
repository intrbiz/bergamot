package com.intrbiz.bergamot.model.message.agent.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

public abstract class AgentError extends AgentMessage
{
    @JsonProperty("message")
    protected String message;
    
    public AgentError()
    {
        super();
    }
    
    public AgentError(String id)
    {
        super(id);
    }
    
    public AgentError(AgentMessage inResponseTo, String message)
    {
        super(inResponseTo);
        this.message = message;
    }
    
    public AgentError(String id, String message)
    {
        super(id);
        this.message = message;
    }

    public final String getMessage()
    {
        return message;
    }

    public final void setMessage(String message)
    {
        this.message = message;
    }
    
    
}
