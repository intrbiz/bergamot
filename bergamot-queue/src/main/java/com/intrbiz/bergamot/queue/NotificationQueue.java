package com.intrbiz.bergamot.queue;


import java.util.UUID;

import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.queue.impl.hcq.HCQNotificationQueue;
import com.intrbiz.bergamot.queue.impl.rabbit.RabbitNotificationQueue;
import com.intrbiz.bergamot.queue.key.NotificationKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;

/**
 * Send notification events
 */
public abstract class NotificationQueue extends QueueAdapter
{    
    static
    {
        RabbitNotificationQueue.register();
        HCQNotificationQueue.register();
    }
    
    public static NotificationQueue open()
    {
        return QueueManager.getInstance().queueAdapter(NotificationQueue.class);
    }
    
    public abstract RoutedProducer<Notification, NotificationKey> publishNotifications(NotificationKey defaultKey);
    
    public RoutedProducer<Notification, NotificationKey> publishNotifications()
    {
        return this.publishNotifications(null);
    }
    
    /**
     * Consume notifications using a queue for the given engine name, 
     * this balances events over multiple consumers for each engine.
     */
    public abstract Consumer<Notification, NotificationKey> consumeNotifications(DeliveryHandler<Notification> handler, UUID site, String engineName);
    
    /**
     * Consume notifications using an ephemeral queue so that 
     * all web notification consumers see all events
     */
    public abstract Consumer<Notification, NotificationKey> consumeNotifications(DeliveryHandler<Notification> handler, UUID site);
}
