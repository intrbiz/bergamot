package com.intrbiz.bergamot.model.message.api.result;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIRequest;

@JsonTypeName("bergamot.api.register_for_adhoc_results")
public class RegisterForAdhocResults extends APIRequest
{
    public RegisterForAdhocResults()
    {
        super();
    }
}
