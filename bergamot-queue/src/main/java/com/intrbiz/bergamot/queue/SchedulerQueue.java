package com.intrbiz.bergamot.queue;


import java.util.UUID;

import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.queue.impl.RabbitSchedulerQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.GenericKey;

/**
 * Send scheduling actiions
 */
public abstract class SchedulerQueue extends QueueAdapter
{    
    static
    {
        RabbitSchedulerQueue.register();
    }
    
    public static SchedulerQueue open()
    {
        return QueueManager.getInstance().queueAdapter(SchedulerQueue.class);
    }
    
    public abstract RoutedProducer<SchedulerAction> publishSchedulerActions(GenericKey defaultKey);
    
    public RoutedProducer<SchedulerAction> publishSchedulerActions()
    {
        return this.publishSchedulerActions(null);
    }
    
    public abstract Consumer<SchedulerAction> consumeSchedulerActions(DeliveryHandler<SchedulerAction> handler, UUID site);
}
