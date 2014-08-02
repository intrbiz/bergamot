package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.error")
public class ErrorMO extends MessageObject
{
    @JsonProperty("message")
    private String message;
    
    public ErrorMO()
    {
        super();
    }
    
    public ErrorMO(String message)
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
