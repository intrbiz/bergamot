package com.intrbiz.bergamot.queue;


import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.queue.impl.RabbitSchedulerQueue;
import com.intrbiz.bergamot.queue.key.SchedulerKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;

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
    
    public abstract RoutedProducer<SchedulerAction, SchedulerKey> publishSchedulerActions(SchedulerKey defaultKey);
    
    public RoutedProducer<SchedulerAction, SchedulerKey> publishSchedulerActions()
    {
        return this.publishSchedulerActions(null);
    }
    
    public abstract Consumer<SchedulerAction, SchedulerKey> consumeSchedulerActions(DeliveryHandler<SchedulerAction> handler);
}
