package com.intrbiz.bergamot.queue.impl.hcq;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.bergamot.queue.key.NotificationKey;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.hcq.client.HCQBatch;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.hcq.HCQConsumer;
import com.intrbiz.queue.hcq.HCQPool;
import com.intrbiz.queue.hcq.HCQProducer;

public class HCQNotificationQueue extends NotificationQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(NotificationQueue.class, HCQPool.TYPE, HCQNotificationQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public HCQNotificationQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "notification-queue";
    }

    @Override
    public RoutedProducer<Notification, NotificationKey> publishNotifications(NotificationKey defaultKey)
    {
        return new HCQProducer<Notification, NotificationKey>(this.broker, this.transcoder.asQueueEventTranscoder(Notification.class), defaultKey, this.source.getRegistry().timer("publish-notifications"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.notification", "topic");
                return "bergamot.notification";
            }
        };
    }

    @Override
    public Consumer<Notification, NotificationKey> consumeNotifications(DeliveryHandler<Notification> handler, UUID site, String engineName)
    {
        return new HCQConsumer<Notification, NotificationKey>(this.broker, this.transcoder.asQueueEventTranscoder(Notification.class), handler, this.source.getRegistry().timer("consume-notifications"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.notification", "topic");
                String queueName = "bergamot.notification." + (site == null ? "default" : site.toString()) + "." + engineName;
                on.getOrCreateQueue(queueName, false);
                on.bindQueueToExchange("bergamot.notification", site == null ? "#" : site.toString(), queueName);
                return queueName;
            }
        };
    }
    
    @Override
    public Consumer<Notification, NotificationKey> consumeNotifications(DeliveryHandler<Notification> handler, UUID site)
    {
        return new HCQConsumer<Notification, NotificationKey>(this.broker, this.transcoder.asQueueEventTranscoder(Notification.class), handler, this.source.getRegistry().timer("consume-notifications"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.notification", "topic");
                String queueName = "notifications-" + UUID.randomUUID();
                on.getOrCreateTempQueue(queueName);
                on.bindQueueToExchange("bergamot.notification", site == null ? "#" : site.toString(), queueName);
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}
