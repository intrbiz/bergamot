package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.queue.name.GenericKey;

public class WorkerKey extends GenericKey
{   
    protected WorkerKey(String key)
    {
        super(key);
    }
    
    public WorkerKey(UUID siteId, String workerPool, String engine, UUID agent)
    {
        super(siteId + "." + Util.coalesceEmpty(workerPool, "any") + "." + engine + "." + Util.coalesce(agent, "any"));
    }
    
    public WorkerKey(UUID siteId, String engine)
    {
        this(siteId, null, engine, null);
    }
}
