package com.intrbiz.bergamot.model.message.processor.result.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match a service based on the trap name and some host match criteria
 */
@JsonTypeName("bergamot.result.match_on.service_name")
public class MatchOnTrapName extends MatchOn
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("host_match")
    private MatchOn hostMatch;

    @JsonProperty("trap_name")
    private String trapName;
    
    public MatchOnTrapName()
    {
        super();
    }

    public MatchOnTrapName(MatchOn hostMatch, String trapName)
    {
        this.hostMatch = hostMatch;
        this.trapName = trapName;
    }

    public MatchOn getHostMatch()
    {
        return hostMatch;
    }

    public void setHostMatch(MatchOn hostMatch)
    {
        this.hostMatch = hostMatch;
    }

    public String getTrapName()
    {
        return trapName;
    }

    public void setTrapName(String trapName)
    {
        this.trapName = trapName;
    }
    
    @Override
    public long routeHash()
    {
        return this.hostMatch.routeHash();
    }
}
