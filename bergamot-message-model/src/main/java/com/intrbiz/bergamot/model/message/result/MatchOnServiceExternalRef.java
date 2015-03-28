package com.intrbiz.bergamot.model.message.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match a service based on the service external ref and some host match criteria
 */
@JsonTypeName("bergamot.result.match_on.service_external_ref")
public class MatchOnServiceExternalRef extends MatchOn
{
    @JsonProperty("host_match")
    private MatchOn hostMatch;

    @JsonProperty("external_ref")
    private String externalRef;
    
    public MatchOnServiceExternalRef()
    {
        super();
    }

    public MatchOnServiceExternalRef(MatchOn hostMatch, String externalRef)
    {
        this.hostMatch = hostMatch;
        this.externalRef = externalRef;
    }

    public MatchOn getHostMatch()
    {
        return hostMatch;
    }

    public void setHostMatch(MatchOn hostMatch)
    {
        this.hostMatch = hostMatch;
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
