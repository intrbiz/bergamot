package com.intrbiz.bergamot.model.message.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;

/**
 * Alter the scheduling
 */
public abstract class SchedulerMessage extends Message
{   
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("pool")
    private int pool;
    
    public SchedulerMessage()
    {
        super();
    }
    
    public SchedulerMessage(int pool)
    {
        super();
        this.pool = pool;
    }

    public int getPool()
    {
        return this.pool;
    }

    public void setPool(int pool)
    {
        this.pool = pool;
    }
}
