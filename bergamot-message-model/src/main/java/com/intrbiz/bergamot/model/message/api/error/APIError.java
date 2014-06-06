package com.intrbiz.bergamot.model.message.api.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIRequest;
import com.intrbiz.bergamot.model.message.api.APIResponse;

@JsonTypeName("bergamot.api.error")
public class APIError extends APIResponse
{
    @JsonProperty("message")
    private String message;
    
    public APIError(APIRequest inResponseTo, String message)
    {
        super(inResponseTo, Stat.ERROR);
        this.message = message;
    }
    
    /* A global error */
    public APIError(String message)
    {
        super();
        this.stat = Stat.ERROR;
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
