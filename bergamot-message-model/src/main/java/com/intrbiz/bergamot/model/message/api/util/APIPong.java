package com.intrbiz.bergamot.model.message.api.util;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIResponse;

@JsonTypeName("bergamot.api.util.pong")
public class APIPong extends APIResponse
{   
    public APIPong()
    {
        super();
    }
    
    public APIPong(APIPing inResponseTo)
    {
        super(inResponseTo, Stat.OK);
    }
    
    public APIPong(APIPing inResponseTo, Stat stat)
    {
        super(inResponseTo, stat);
    }
}
