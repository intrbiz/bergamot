package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.result.ResultMO;

public class ProcessingPoolConsumer
{
    
    private final HazelcastInstance hazelcast;
    
    private final UUID poolId;
    
    private final IQueue<ResultMO> resultQueue;
    
    public ProcessingPoolConsumer(HazelcastInstance hazelcast, UUID poolId)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.poolId = Objects.requireNonNull(poolId);
        // Create our queues
        this.resultQueue = this.hazelcast.getQueue(ObjectNames.buildResultQueueName(this.poolId));
    }
    
    public ResultMO pollResult()
    {
        try
        {
            return this.resultQueue.poll(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException ie)
        {
            // ignore
        }
        return null;
    }
}
