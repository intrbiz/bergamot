package com.intrbiz.bergamot.cluster.broker;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.hazelcast.collection.impl.queue.QueueService;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.model.message.MessageObject;

public abstract class GenericPartitionedQueue<K, T extends MessageObject>
{
    protected final HazelcastInstance hazelcast;
    
    protected final Function<K, String> queueName;
    
    protected final ConcurrentMap<String, IQueue<T>> queues;

    protected GenericPartitionedQueue(HazelcastInstance hazelcast, Function<K, String> queueName)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.queueName = Objects.requireNonNull(queueName);
        // Create our topic
        this.queues = new ConcurrentHashMap<>();
        // Setup a object listener to clean up our siteTopics cache
        this.hazelcast.addDistributedObjectListener(new QueueListener());
    }
    
    private IQueue<T> getSiteTopic(K siteId)
    {
        return this.queues.computeIfAbsent(this.queueName.apply(siteId), this.hazelcast::getQueue);
    }
    
    public boolean offer(K key, T message)
    {
        return this.getSiteTopic(key).offer(message);
    }
    
    public T peek(K key)
    {
        return this.getSiteTopic(key).peek();
    }
    
    public T poll(K key)
    {
        return this.getSiteTopic(key).poll();
    }
    
    public T poll(K key, long timeout, TimeUnit unit) throws InterruptedException
    {
        return this.getSiteTopic(key).poll(timeout, unit);
    }
    
    private class QueueListener implements DistributedObjectListener
    {
        @Override
        public void distributedObjectCreated(DistributedObjectEvent event)
        {
        }

        @Override
        public void distributedObjectDestroyed(DistributedObjectEvent event)
        {
            if (QueueService.SERVICE_NAME.equals(event.getServiceName()))
            {
                GenericPartitionedQueue.this.queues.remove(event.getObjectName());
            }
        }
    }
}
