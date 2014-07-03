package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.queue.name.GenericKey;

public class SchedulerKey extends GenericKey
{    
    public SchedulerKey(UUID site, int pool)
    {
        super(site + "." + pool);
    }
}
