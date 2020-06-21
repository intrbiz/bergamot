package com.intrbiz.bergamot.model.message.agent.error;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.error.general")
public class GeneralError extends AgentError
{
    private static final long serialVersionUID = 1L;

    public GeneralError()
    {
        super();
    }

    public GeneralError(Message inResponseTo, String message)
    {
        super(inResponseTo, message);
    }

    public GeneralError(String message)
    {
        super(message);
    }
}
