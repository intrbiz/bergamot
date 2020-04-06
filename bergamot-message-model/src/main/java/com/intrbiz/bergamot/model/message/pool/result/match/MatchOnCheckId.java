package com.intrbiz.bergamot.model.message.pool.result.match;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * Match on the check id
 */
@JsonTypeName("bergamot.result.match_on.check_id")
public class MatchOnCheckId extends MatchOn
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("check_type")
    private String checkType;

    @JsonProperty("check_id")
    private UUID checkId;
    
    public MatchOnCheckId()
    {
        super();
    }

    public MatchOnCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public MatchOnCheckId(String checkType, UUID checkId)
    {
        this.checkType = checkType;
        this.checkId = checkId;
    }

    public String getCheckType()
    {
        return checkType;
    }

    public void setCheckType(String checkType)
    {
        this.checkType = checkType;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }
    
    @Override
    public int getPool()
    {
        return CheckMO.computePool(this.checkId);
    }
}
