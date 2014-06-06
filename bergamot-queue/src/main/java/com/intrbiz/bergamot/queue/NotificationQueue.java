package com.intrbiz.bergamot.queue;


import java.util.UUID;

import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.queue.impl.RabbitNotificationQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.GenericKey;

/**
 * Send notification events
 */
public abstract class NotificationQueue extends QueueAdapter
{    
    static
    {
        RabbitNotificationQueue.register();
    }
    
    public static NotificationQueue open()
    {
        return QueueManager.getInstance().queueAdapter(NotificationQueue.class);
    }
    
    public abstract RoutedProducer<Notification> publishNotifications(GenericKey defaultKey);
    
    public RoutedProducer<Notification> publishNotifications()
    {
        return this.publishNotifications(null);
    }
    
    public abstract Consumer<Notification> consumeNotifications(DeliveryHandler<Notification> handler, UUID site, String engineName);
}
