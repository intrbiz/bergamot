package com.intrbiz.bergamot.queue.key;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.queue.name.GenericKey;

public class WorkerKey extends GenericKey
{    
    public WorkerKey(UUID siteId, String workerPool, String engine)
    {
        super(siteId + "." + Util.coalesceEmpty(workerPool, "any") + "." + engine);
    }
    
    public WorkerKey(UUID siteId, String engine)
    {
        this(siteId, null, engine);
    }
}
