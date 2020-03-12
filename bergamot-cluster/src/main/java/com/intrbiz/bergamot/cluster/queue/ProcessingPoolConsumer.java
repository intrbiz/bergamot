package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;

/**
 * Consume result and reading to be processed
 */
public class ProcessingPoolConsumer
{
    private final HazelcastInstance hazelcast;
    
    private final UUID poolId;
    
    private final IQueue<ResultMO> resultQueue;
    
    private final IQueue<ReadingParcelMO> readingQueue;
    
    public ProcessingPoolConsumer(HazelcastInstance hazelcast, UUID poolId)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.poolId = Objects.requireNonNull(poolId);
        // Create our queues
        this.resultQueue = this.hazelcast.getQueue(ObjectNames.buildResultQueueName(this.poolId));
        this.readingQueue = this.hazelcast.getQueue(ObjectNames.buildReadingQueueName(this.poolId));
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
    
    public ReadingParcelMO pollReading()
    {
        try
        {
            return this.readingQueue.poll(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException ie)
        {
            // ignore
        }
        return null;
    }
}
