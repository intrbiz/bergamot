package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.coordinator.WorkerClientCoordinator;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

public class WorkerConsumer
{   
    private static final long POKE_THRESHOLD = TimeUnit.SECONDS.toNanos(10);
    
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
    
    private volatile boolean closed;
    
    private volatile long lastPoke = 0L;
    
    public WorkerConsumer(HazelcastInstance hazelcast, UUID workerId, WorkerClientCoordinator coordinator)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.workerId = Objects.requireNonNull(workerId);
        this.coordinator = Objects.requireNonNull(coordinator);
        this.closed = false;
        // Get the check queue
        this.checkQueue = this.hazelcast.getQueue(ObjectNames.buildCheckQueueName(this.workerId));
    }
    
    public UUID getWorkerId()
    {
        return this.workerId;
    }
    
    // Aliveness
    
    protected void pokeWatchDog()
    {
        long now = System.nanoTime();
        if ((now - this.lastPoke) > POKE_THRESHOLD)
        {
            this.lastPoke = now;
            // TODO: Poke Poke Poke
        }
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
    
    public void close()
    {
        // unregister
        this.coordinator.unregisterWorker(this.workerId);
        // close this queue
        this.checkQueue.destroy();
    }
}
