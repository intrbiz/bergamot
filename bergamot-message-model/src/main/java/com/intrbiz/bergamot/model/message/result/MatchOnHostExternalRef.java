package com.intrbiz.bergamot.model.message.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match a host by its external ref
 */
@JsonTypeName("bergamot.result.match_on.host_external_ref")
public class MatchOnHostExternalRef extends MatchOn
{
    @JsonProperty("external_ref")
    private String externalRef;
    
    public MatchOnHostExternalRef()
    {
        super();
    }
    
    public MatchOnHostExternalRef(String externalRef)
    {
        this.externalRef = externalRef;
    }

    public String getExternalRef()
    {
        return externalRef;
    }

    public void setExternalRef(String externalRef)
    {
        this.externalRef = externalRef;
    }
}
