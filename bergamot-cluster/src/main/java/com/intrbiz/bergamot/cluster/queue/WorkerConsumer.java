package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

public abstract class WorkerConsumer
{   
    private static final long POKE_THRESHOLD = TimeUnit.SECONDS.toNanos(3);
    
    /**
     * Our dedicated queue to process work form
     */
    protected final IQueue<ExecuteCheck> checkQueue;
    
    protected volatile boolean closed;
    
    protected volatile long lastPoke = 0L;
    
    public WorkerConsumer(IQueue<ExecuteCheck> checkQueue)
    {
        super();
        this.checkQueue = Objects.requireNonNull(checkQueue);
    }
    
    // Aliveness
    
    protected void pokeWatchDog()
    {
        long now = System.nanoTime();
        if ((now - this.lastPoke) > POKE_THRESHOLD)
        {
            this.lastPoke = now;
            // Poke the dog
            this.updateWatchDog();
        }
    }
    
    protected abstract void updateWatchDog();
    
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
        this.unregister();
    }
    
    protected abstract void unregister();
}
