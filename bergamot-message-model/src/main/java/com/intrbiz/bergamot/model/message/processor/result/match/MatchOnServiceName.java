package com.intrbiz.bergamot.model.message.processor.result.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match a service based on the service name and some host match criteria
 */
@JsonTypeName("bergamot.result.match_on.service_name")
public class MatchOnServiceName extends MatchOn
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("host_match")
    private MatchOn hostMatch;

    @JsonProperty("service_name")
    private String serviceName;
    
    public MatchOnServiceName()
    {
        super();
    }

    public MatchOnServiceName(MatchOn hostMatch, String serviceName)
    {
        this.hostMatch = hostMatch;
        this.serviceName = serviceName;
    }

    public MatchOn getHostMatch()
    {
        return hostMatch;
    }

    public void setHostMatch(MatchOn hostMatch)
    {
        this.hostMatch = hostMatch;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }
    
    @Override
    public long routeHash()
    {
        return this.hostMatch.routeHash();
    }
}
