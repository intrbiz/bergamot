package com.intrbiz.bergamot.model.message.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class APIRequest extends APIObject
{
    @JsonProperty("request_id")
    protected String requestId;
    
    public APIRequest()
    {
        super();
    }
    
    public APIRequest(String requestId)
    {
        super();
        this.requestId = requestId;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }
}
