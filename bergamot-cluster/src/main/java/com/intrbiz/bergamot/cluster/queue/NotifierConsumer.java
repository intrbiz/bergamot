package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.notification.Notification;

public class NotifierConsumer
{   
    private static final long POKE_THRESHOLD = TimeUnit.SECONDS.toNanos(3);
    
    /**
     * The hazelcast instance to use
     */
    private final HazelcastInstance hazelcast;
    
    /**
     * Our notifier id
     */
    private final UUID notifierId;
    
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
    private final IQueue<Notification> queue;
    
    private volatile boolean closed;
    
    private volatile long lastPoke = 0L;
    
    public NotifierConsumer(HazelcastInstance hazelcast, UUID notifierId, Consumer<UUID> onClose, Consumer<UUID> watchdog)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.notifierId = Objects.requireNonNull(notifierId);
        this.onClose = Objects.requireNonNull(onClose);
        this.watchdog = Objects.requireNonNull(watchdog);
        this.closed = false;
        // Get the check queue
        this.queue = this.hazelcast.getQueue(ObjectNames.buildNotifierQueueName(this.notifierId));
    }
    
    public UUID getWorkerId()
    {
        return this.notifierId;
    }
    
    // Aliveness
    
    protected void pokeWatchDog()
    {
        long now = System.nanoTime();
        if ((now - this.lastPoke) > POKE_THRESHOLD)
        {
            this.lastPoke = now;
            // Poke the dog
            this.watchdog.accept(this.notifierId);
        }
    }
    
    // Check handling
    
    public Notification poll()
    {
        if (this.closed) return null;
        try
        {
            this.pokeWatchDog();
            return this.queue.poll(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            // ignore
        }
        return null;
    }
    
    public void close()
    {
        this.onClose.accept(this.notifierId);
    }
}
