package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.queue.name.GenericKey;

public class ResultKey extends GenericKey
{    
    public ResultKey(UUID site, int pool)
    {
        super(site + "." + pool);
    }
    
    /**
     * Create a ResultKey from just the check id.
     * 
     * Note this always sends the result to processing 
     * pool 0.
     */
    public ResultKey(UUID objectId)
    {
        this(new UUID((objectId.getMostSignificantBits() & 0xFFFFFFFF_FFFF0000L) | 0x0000000000004000L, 0x80000000_00000000L), 0);
    }
}
