package com.intrbiz.bergamot.model.message.api.util;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIRequest;

@JsonTypeName("bergamot.api.util.ping")
public class APIPing extends APIRequest
{   
    public APIPing()
    {
        super();
    }
}
