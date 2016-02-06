package com.intrbiz.bergamot.model.message.api.result;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIResponse;

@JsonTypeName("bergamot.api.registered_for_adhoc_results")
public class RegisteredForAdhocResults extends APIResponse
{
    @JsonProperty("adhoc_id")
    private UUID adhocId;
    
    public RegisteredForAdhocResults(RegisterForAdhocResults inResponseTo, UUID adhocId)
    {
        super(inResponseTo, Stat.OK);
        this.adhocId = adhocId;
    }

    public UUID getAdhocId()
    {
        return adhocId;
    }

    public void setAdhocId(UUID adhocId)
    {
        this.adhocId = adhocId;
    }
}
