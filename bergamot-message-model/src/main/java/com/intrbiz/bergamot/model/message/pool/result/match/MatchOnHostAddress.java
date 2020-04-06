package com.intrbiz.bergamot.model.message.pool.result.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * Match a host by its address
 */
@JsonTypeName("bergamot.result.match_on.host_address")
public class MatchOnHostAddress extends MatchOn
{
    private static final long serialVersionUID = 1L;
    
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
    
    @Override
    public int getPool()
    {
        // Try to consistently send the same check to the same pool
        return this.hostAddress.hashCode() % CheckMO.PROCESSING_POOL_COUNT;
    }
}
