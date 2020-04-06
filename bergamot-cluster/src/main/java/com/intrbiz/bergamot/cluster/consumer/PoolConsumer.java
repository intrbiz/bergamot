package com.intrbiz.bergamot.cluster.consumer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.pool.PoolMessage;
import com.intrbiz.bergamot.model.message.pool.result.ResultMessage;

public class PoolConsumer
{   
    private final HazelcastInstance hazelcast;
    
    private final int pool;
    
    private final IQueue<ResultMessage> queue;
    
    public PoolConsumer(HazelcastInstance hazelcast, int pool)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.pool = pool;
        this.queue = this.hazelcast.getQueue(HZNames.buildPoolQueueName(this.pool));
    }
    
    public PoolMessage poll(long timeout, TimeUnit unit)
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
