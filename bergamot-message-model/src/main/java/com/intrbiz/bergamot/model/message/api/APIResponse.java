package com.intrbiz.bergamot.model.message.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class APIResponse extends APIObject
{
    public enum Stat { OK, ERROR }
    
    @JsonProperty("in_response_to")
    protected String inResponseTo;
    
    @JsonProperty("stat")
    protected Stat stat = Stat.OK;
    
    public APIResponse()
    {
        super();
    }
    
    public APIResponse(Stat stat)
    {
        super();
        this.stat = stat;
    }
    
    public APIResponse(APIRequest inResponseTo, Stat stat)
    {
        super();
        this.inResponseTo = inResponseTo.getRequestId();
        this.stat = stat;
    }

    public Stat getStat()
    {
        return stat;
    }

    public void setStat(Stat stat)
    {
        this.stat = stat;
    }
    
    
}
