package com.intrbiz.bergamot.model.message.processor.result.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match a trap based on the trap external ref and some host match criteria
 */
@JsonTypeName("bergamot.result.match_on.trap_external_ref")
public class MatchOnTrapExternalRef extends MatchOn
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("host_match")
    private MatchOn hostMatch;

    @JsonProperty("external_ref")
    private String externalRef;
    
    public MatchOnTrapExternalRef()
    {
        super();
    }

    public MatchOnTrapExternalRef(MatchOn hostMatch, String externalRef)
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
    
    @Override
    public long routeHash()
    {
        return this.hostMatch.routeHash();
    }
}
