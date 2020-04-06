package com.intrbiz.bergamot.model.message.pool.result.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * Match a host by its external ref
 */
@JsonTypeName("bergamot.result.match_on.host_external_ref")
public class MatchOnHostExternalRef extends MatchOn
{
    private static final long serialVersionUID = 1L;
    
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
    
    @Override
    public int getPool()
    {
        // Try to consistently send the same check to the same pool
        return this.externalRef.hashCode() % CheckMO.PROCESSING_POOL_COUNT;
    }
}
