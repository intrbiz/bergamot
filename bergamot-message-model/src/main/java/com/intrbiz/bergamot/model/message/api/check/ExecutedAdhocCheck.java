package com.intrbiz.bergamot.model.message.api.check;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.api.APIResponse;

@JsonTypeName("bergamot.api.executed_adhoc_check")
public class ExecutedAdhocCheck extends APIResponse
{
    private UUID checkId;
    
    public ExecutedAdhocCheck(ExecuteAdhocCheck inResponseTo, UUID checkId)
    {
        super(inResponseTo, Stat.OK);
        this.checkId = checkId;
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
