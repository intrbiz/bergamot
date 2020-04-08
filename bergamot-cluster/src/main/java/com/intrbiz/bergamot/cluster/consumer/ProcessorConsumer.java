package com.intrbiz.bergamot.cluster.consumer;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;

public class ProcessorConsumer
{   
    private final HazelcastInstance hazelcast;
    
    private final UUID id;
    
    private final IQueue<ProcessorMessage> queue;
    
    public ProcessorConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.id = id;
        this.queue = this.hazelcast.getQueue(HZNames.buildProcessorQueueName(this.id));
    }
    
    public UUID getId()
    {
        return this.id;
    }
    
    public ProcessorMessage poll(long timeout, TimeUnit unit)
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
