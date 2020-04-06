package com.intrbiz.bergamot.model.message.pool.scheduler;

import com.intrbiz.bergamot.model.message.pool.PoolMessage;

/**
 * Alter the scheduling
 */
public abstract class SchedulerMessage extends PoolMessage
{   
    private static final long serialVersionUID = 1L;
    
    public SchedulerMessage()
    {
        super();
    }
}
