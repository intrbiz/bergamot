package com.intrbiz.bergamot.model.message.pool.result.match;

import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * Criteria for matching passive results to checks
 */
public abstract class MatchOn extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    public MatchOn()
    {
        super();
    }
    
    public abstract int getPool();
}
