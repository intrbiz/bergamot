package com.intrbiz.bergamot.model.message.agent.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;

public abstract class AgentError extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("message")
    protected String message;
    
    public AgentError()
    {
        super();
    }
    
    public AgentError(Message inResponseTo, String message)
    {
        super(inResponseTo);
        this.message = message;
    }
    
    public AgentError(String message)
    {
        super();
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
