package com.intrbiz.bergamot.model.message.agent.error;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.error.general")
public class GeneralError extends AgentError
{
    public GeneralError()
    {
        super();
    }
    
    public GeneralError(String id)
    {
        super(id);
    }

    public GeneralError(AgentMessage inResponseTo, String message)
    {
        super(inResponseTo, message);
    }

    public GeneralError(String id, String message)
    {
        super(id, message);
    }
}
