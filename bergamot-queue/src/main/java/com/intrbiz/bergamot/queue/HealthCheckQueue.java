package com.intrbiz.bergamot.queue;


import com.intrbiz.bergamot.model.message.health.HealthCheckMessage;
import com.intrbiz.bergamot.queue.impl.RabbitHealthCheckQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.name.NullKey;

/**
 * Publish and consume internal health check events
 * 
 */
public abstract class HealthCheckQueue extends QueueAdapter
{    
    static
    {
        RabbitHealthCheckQueue.register();
    }
    
    public static HealthCheckQueue open()
    {
        return QueueManager.getInstance().queueAdapter(HealthCheckQueue.class);
    }
    
    // control events
    
    public abstract Producer<HealthCheckMessage> publishHealthCheckEvents();
    
    public abstract Producer<HealthCheckMessage> publishHealthCheckControlEvents();
    
    public abstract Consumer<HealthCheckMessage, NullKey> consumeHealthCheckEvents(DeliveryHandler<HealthCheckMessage> handler);
    
    public abstract Consumer<HealthCheckMessage, NullKey> consumeHealthCheckControlEvents(DeliveryHandler<HealthCheckMessage> handler);
}
