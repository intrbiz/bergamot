package com.intrbiz.bergamot.queue;


import java.util.Set;
import java.util.UUID;

import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.impl.RabbitUpdateQueue;
import com.intrbiz.bergamot.queue.key.UpdateKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;

/**
 * Send update events
 */
public abstract class UpdateQueue extends QueueAdapter
{    
    static
    {
        RabbitUpdateQueue.register();
    }
    
    public static UpdateQueue open()
    {
        return QueueManager.getInstance().queueAdapter(UpdateQueue.class);
    }
    
    public abstract RoutedProducer<Update, UpdateKey> publishUpdates(UpdateKey defaultKey);
    
    public RoutedProducer<Update, UpdateKey> publishUpdates()
    {
        return this.publishUpdates(null);
    }
    
    public abstract Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler, UUID site, UUID check);
    
    public abstract Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler);
    
    public abstract Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler, Set<UpdateKey> bindings);
}
