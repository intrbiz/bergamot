package com.intrbiz.bergamot.cluster.consumer;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.pool.check.ExecuteCheck;

public class WorkerConsumer
{   
    private final HazelcastInstance hazelcast;
    
    private final UUID id;
    
    private final IQueue<ExecuteCheck> queue;
    
    public WorkerConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.id = Objects.requireNonNull(id);
        this.queue = this.hazelcast.getQueue(HZNames.buildWorkerQueueName(this.id));
    }
    
    public ExecuteCheck poll(long timeout, TimeUnit unit)
    {
        try
        {
            return this.queue.poll(timeout, unit);
        }
        catch (InterruptedException e)
        {
            // ignore
        }
        return null;
    }
}
