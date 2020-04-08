package com.intrbiz.bergamot.cluster.consumer;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.hazelcast.collection.IQueue;
import com.hazelcast.collection.ItemEvent;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerMessage;

public class SchedulingPoolConsumer
{   
    private final HazelcastInstance hazelcast;
    
    private final int pool;
    
    private final IQueue<SchedulerMessage> queue;
    
    private final ConcurrentMap<UUID, Consumer<SchedulerMessage>> listeners = new ConcurrentHashMap<>();
    
    public SchedulingPoolConsumer(HazelcastInstance hazelcast, int pool)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.pool = pool;
        this.queue = this.hazelcast.getQueue(HZNames.buildSchedulingPoolQueueName(this.pool));
    }
    
    public SchedulerMessage poll(long timeout, TimeUnit unit)
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
    
    public UUID listen(Consumer<SchedulerMessage> consumer)
    {
        UUID id = this.queue.addItemListener(new ItemListener<SchedulerMessage>() {

            @Override
            public void itemAdded(ItemEvent<SchedulerMessage> item)
            {
                consumer.accept(item.getItem());
            }

            @Override
            public void itemRemoved(ItemEvent<SchedulerMessage> item)
            {
            }
            
        }, true);
        this.listeners.put(id,  consumer);
        return id;
    }
    
    public void unlisten(UUID id)
    {
        this.listeners.remove(id);
        this.queue.removeItemListener(id);
    }
    
    public void unlistenAll()
    {
        for (UUID id : this.listeners.keySet())
        {
            this.queue.removeItemListener(id);
        }
        this.listeners.clear();
    }
}
