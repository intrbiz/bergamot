package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.queue.name.GenericKey;

public class ResultKey extends GenericKey
{    
    public ResultKey(UUID site, int pool)
    {
        super(site + "." + pool);
    }
}
