package com.intrbiz.bergamot.model.message.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;

/**
 * A state update
 */
public abstract class Update extends Message
{
    @JsonProperty("updated_at")
    private long updatedAt;

    public Update()
    {
        super();
    }
    
    public Update(long updatedAt)
    {
        super();
        this.updatedAt = updatedAt;
    }

    public long getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt)
    {
        this.updatedAt = updatedAt;
    }
}
