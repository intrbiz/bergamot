package com.intrbiz.bergamot.model.message.pool.result.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.CheckMO;

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
    public int getPool()
    {
        // Try to consistently send the same check to the same pool
        return this.hostName.hashCode() % CheckMO.PROCESSING_POOL_COUNT;
    }
}
