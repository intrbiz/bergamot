package com.intrbiz.bergamot.queue;


import java.util.UUID;

import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.queue.impl.RabbitWatcherQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;

/**
 * Send events to watchers
 */
public abstract class WatcherQueue extends QueueAdapter
{    
    static
    {
        RabbitWatcherQueue.register();
    }
    
    public static WatcherQueue open()
    {
        return QueueManager.getInstance().queueAdapter(WatcherQueue.class);
    }
    
    // watcher
    
    public abstract RoutedProducer<CheckEvent> publishWatcherEvents();
    
    public abstract Consumer<CheckEvent> consumeWatcherEvents(DeliveryHandler<CheckEvent> handler, UUID watcher, String engine);
}
