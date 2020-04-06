package com.intrbiz.bergamot.model.message.pool;

import com.intrbiz.bergamot.model.message.Message;


/**
 * A message which is directed to a processing pool
 */
public abstract class PoolMessage extends Message
{
    private static final long serialVersionUID = 1L;

    public PoolMessage()
    {
        super();
    }
    
    public abstract int getPool();
}
