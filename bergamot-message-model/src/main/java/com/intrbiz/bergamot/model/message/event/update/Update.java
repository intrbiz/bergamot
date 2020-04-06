package com.intrbiz.bergamot.model.message.event.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.event.Event;

/**
 * A state update
 */
public abstract class Update extends Event
{
    private static final long serialVersionUID = 1L;
    
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
