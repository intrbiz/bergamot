package com.intrbiz.bergamot.model.message.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.Message;

/**
 * A state update
 */
@JsonTypeName("bergamot.update")
public class Update extends Message
{
    @JsonProperty("updated_at")
    private long updatedAt;

    @JsonProperty("check")
    private CheckMO check;

    public Update()
    {
        super();
    }
    
    public Update(CheckMO check)
    {
        super();
        this.updatedAt = System.currentTimeMillis();
        this.check = check;
    }

    public long getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt)
    {
        this.updatedAt = updatedAt;
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
