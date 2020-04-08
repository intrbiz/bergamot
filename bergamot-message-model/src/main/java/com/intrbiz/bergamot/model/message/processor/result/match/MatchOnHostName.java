package com.intrbiz.bergamot.model.message.processor.result.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match a host by its name
 */
@JsonTypeName("bergamot.result.match_on.host_name")
public class MatchOnHostName extends MatchOn
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("host_name")
    private String hostName;
    
    public MatchOnHostName()
    {
        super();
    }
    
    public MatchOnHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }
    
    @Override
    public long routeHash()
    {
        return this.hostName.hashCode();
    }
}
