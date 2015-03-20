package com.intrbiz.bergamot.model.message.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match a host by its address
 */
@JsonTypeName("bergamot.result.match_on.host_address")
public class MatchOnHostAddress extends MatchOn
{
    @JsonProperty("host_address")
    private String hostAddress;
    
    public MatchOnHostAddress()
    {
        super();
    }
    
    public MatchOnHostAddress(String hostAddress)
    {
        this.hostAddress = hostAddress;
    }

    public String getHostAddress()
    {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress)
    {
        this.hostAddress = hostAddress;
    }
}
