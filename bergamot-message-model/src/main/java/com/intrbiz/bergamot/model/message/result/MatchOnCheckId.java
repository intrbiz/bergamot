package com.intrbiz.bergamot.model.message.result;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Match on the check id
 */
@JsonTypeName("bergamot.result.match_on.check_id")
public class MatchOnCheckId extends MatchOn
{
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
}
