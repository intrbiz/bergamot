package com.intrbiz.bergamot.cluster.consumer;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.notification.Notification;

public class NotificationConsumer
{   
    private final HazelcastInstance hazelcast;
    
    private final UUID id;
    
    private final IQueue<Notification> queue;
    
    public NotificationConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.id = Objects.requireNonNull(id);
        this.queue = this.hazelcast.getQueue(HZNames.buildNotifierQueueName(this.id));
    }
    
    public Notification poll(long timeout, TimeUnit unit)
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
