package com.intrbiz.bergamot.cluster.broker;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.model.message.MessageObject;

public abstract class GenericGlobalQueue<T extends MessageObject>
{
    protected final HazelcastInstance hazelcast;
    
    protected final IQueue<T> queue;

    protected GenericGlobalQueue(HazelcastInstance hazelcast, String queueName)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        // Create our queue
        this.queue = this.hazelcast.getQueue(queueName);
    }
    
    public boolean offer(T message)
    {
        return this.queue.offer(message);
    }
    
    public T peek()
    {
        return this.queue.peek();
    }
    
    public T poll()
    {
        return this.queue.poll();
    }
    
    public T poll(long timeout, TimeUnit unit) throws InterruptedException
    {
        return this.queue.poll(timeout, unit);
    }
}
