package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

public class WorkerConsumer
{   
    private static final long POKE_THRESHOLD = TimeUnit.SECONDS.toNanos(3);
    
    /**
     * The hazelcast instance to use
     */
    private final HazelcastInstance hazelcast;
    
    /**
     * Our worker id
     */
    private final UUID workerId;
    
    /**
     * On Close callback
     */
    private final Consumer<UUID> onClose;
    
    /**
     * Watchdog to notify routinely
     */
    private final Consumer<UUID> watchdog;
    
    /**
     * Our dedicated queue to process work form
     */
    private final IQueue<ExecuteCheck> checkQueue;
    
    private volatile boolean closed;
    
    private volatile long lastPoke = 0L;
    
    public WorkerConsumer(HazelcastInstance hazelcast, UUID workerId, Consumer<UUID> onClose, Consumer<UUID> watchdog)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.workerId = Objects.requireNonNull(workerId);
        this.onClose = Objects.requireNonNull(onClose);
        this.watchdog = Objects.requireNonNull(watchdog);
        this.closed = false;
        // Get the check queue
        this.checkQueue = this.hazelcast.getQueue(ObjectNames.buildWorkerQueueName(this.workerId));
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
            // Poke the dog
            this.watchdog.accept(this.workerId);
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
        this.onClose.accept(this.workerId);
    }
}
