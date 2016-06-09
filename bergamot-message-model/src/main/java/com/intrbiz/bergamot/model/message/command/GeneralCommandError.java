package com.intrbiz.bergamot.model.message.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.command.general-error")
public class GeneralCommandError extends CommandResponse
{
    @JsonProperty("message")
    private String message;

    public GeneralCommandError()
    {
        super();
    }
    
    public GeneralCommandError(String message)
    {
        super();
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
