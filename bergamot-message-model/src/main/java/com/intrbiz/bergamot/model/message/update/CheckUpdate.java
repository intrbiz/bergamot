package com.intrbiz.bergamot.model.message.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * A check state update
 */
@JsonTypeName("bergamot.check_update")
public class CheckUpdate extends Update
{
    @JsonProperty("check")
    private CheckMO check;

    public CheckUpdate()
    {
        super();
    }
    
    public CheckUpdate(CheckMO check)
    {
        super(System.currentTimeMillis());
        this.check = check;
    }

    public CheckMO getCheck()
    {
        return check;
    }

    public void setCheck(CheckMO check)
    {
        this.check = check;
    }
}
