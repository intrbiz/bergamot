package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.coordinator.ProcessingPoolCoordinator;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;

public class ProcessingPoolProducer
{    
    /**
     * The hazelcast instance to use
     */
    private final HazelcastInstance hazelcast;
    
    private final ProcessingPoolCoordinator coordinator;
    
    /**
     * Our cache of result queues
     */
    private final ConcurrentMap<UUID, IQueue<ResultMO>> resultQueues;
    
    /**
     * Our cache of reading queues
     */
    private final ConcurrentMap<UUID, IQueue<ReadingParcelMO>> readingQueues;
    
    public ProcessingPoolProducer(HazelcastInstance hazelcast, ProcessingPoolCoordinator coordinator)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.coordinator = Objects.requireNonNull(coordinator);
        // Setup result and reading queue caches
        this.resultQueues = new ConcurrentHashMap<>();
        this.readingQueues = new ConcurrentHashMap<>();
    }
    
    // Result handling
    
    private IQueue<ResultMO> getResultQueue(UUID pool)
    {
        return this.resultQueues.computeIfAbsent(pool, (key) -> {
            return this.hazelcast.getQueue(ObjectNames.buildResultQueueName(key));
        });
    }
    
    public boolean publishResult(UUID pool, ResultMO resultMO)
    {
        if (pool == null)
        {
            pool = this.coordinator.routePassiveCheck(resultMO.getSiteId());
        }
        IQueue<ResultMO> queue = this.getResultQueue(pool);
        try
        {
            return queue.offer(resultMO, 10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        }
        return false;
    }
    
    public boolean publishResult(ResultMO resultMO)
    {
        return this.publishResult(null, resultMO);
    }
    
    // Reading handling
    
    private IQueue<ReadingParcelMO> getReadingQueue(UUID pool)
    {
        return this.readingQueues.computeIfAbsent(pool, (key) -> {
            return this.hazelcast.getQueue(ObjectNames.buildReadingQueueName(key));
        });
    }
    
    public boolean publishReading(UUID pool, ReadingParcelMO readingParcelMO)
    {
        if (pool == null)
        {
            pool = this.coordinator.routePassiveCheck(readingParcelMO.getSiteId());
        }
        IQueue<ReadingParcelMO> queue = this.getReadingQueue(pool);
        try
        {
            return queue.offer(readingParcelMO, 10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
        }
        return false;
    }
    
    public boolean publishReading(ReadingParcelMO readingParcelMO)
    {
        return this.publishReading(null, readingParcelMO);
    }
}
