package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.coordinator.WorkerClientCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.ClusterNames;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;

public class WorkerConsumer
{   
    /**
     * The hazelcast instance to use
     */
    private final HazelcastInstance hazelcast;
    
    /**
     * Our worker id
     */
    private final UUID workerId;
    
    /**
     * The coordinator which created us
     */
    private final WorkerClientCoordinator coordinator;
    
    /**
     * Our dedicated queue to process work form
     */
    private final IQueue<ExecuteCheck> checkQueue;
    
    /**
     * Our cache of result queues
     */
    private final ConcurrentMap<UUID, IQueue<ResultMO>> resultQueues;
    
    /**
     * Our cache of reading queues
     */
    private final ConcurrentMap<UUID, IQueue<ReadingParcelMO>> readingQueues;
    
    private volatile boolean closed;
    
    public WorkerConsumer(HazelcastInstance hazelcast, UUID workerId, WorkerClientCoordinator coordinator)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.workerId = Objects.requireNonNull(workerId);
        this.coordinator = Objects.requireNonNull(coordinator);
        this.closed = false;
        // Get the check queue
        this.checkQueue = this.hazelcast.getQueue(ClusterNames.buildCheckQueueName(this.workerId));
        // Setup result and reading queue caches
        this.resultQueues = new ConcurrentHashMap<>();
        this.readingQueues = new ConcurrentHashMap<>();
    }
    
    public UUID getWorkerId()
    {
        return this.workerId;
    }
    
    // Alive ness
    
    protected void pokeWatchDog()
    {
        // TODO
    }
    
    // Check handling
    
    public ExecuteCheck poll()
    {
        if (this.closed) return null;
        try
        {
            this.pokeWatchDog();
            return this.checkQueue.poll(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            // ignore
        }
        return null;
    }
    
    // Result handling
    
    private IQueue<ResultMO> getResultQueue(UUID pool)
    {
        return this.resultQueues.computeIfAbsent(pool, (key) -> {
            return this.hazelcast.getQueue(ClusterNames.buildResultQueueName(key));
        });
    }
    
    public boolean publishResult(UUID pool, ResultMO resultMO)
    {
        if (this.closed) return false;
        if (pool == null) pool = this.coordinator.route();
        IQueue<ResultMO> queue = this.getResultQueue(pool);
        try
        {
            this.pokeWatchDog();
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
            return this.hazelcast.getQueue(ClusterNames.buildReadingQueueName(key));
        });
    }
    
    public boolean publishReading(UUID pool, ReadingParcelMO readingParcelMO)
    {
        if (this.closed) return false;
        if (pool == null) pool = this.coordinator.route();
        IQueue<ReadingParcelMO> queue = this.getReadingQueue(pool);
        try
        {
            this.pokeWatchDog();
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
    
    public void close()
    {
        // unregister
        this.coordinator.unregisterWorker(this.workerId);
        // close this queue
        this.checkQueue.destroy();
        // clean up cached queues
        this.readingQueues.clear();
        this.resultQueues.clear();
    }
}
