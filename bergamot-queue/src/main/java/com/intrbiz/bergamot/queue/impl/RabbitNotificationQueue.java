package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

public class RabbitNotificationQueue extends NotificationQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(NotificationQueue.class, RabbitNotificationQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public RabbitNotificationQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "notification-queue";
    }

    @Override
    public RoutedProducer<Notification> publishNotifications(GenericKey defaultKey)
    {
        return new RabbitProducer<Notification>(this.broker, this.transcoder.asQueueEventTranscoder(Notification.class), defaultKey, this.source.getRegistry().timer("publish-notifications"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.notification", "topic", true);
                return "bergamot.notification";
            }
        };
    }

    @Override
    public Consumer<Notification> consumeNotifications(DeliveryHandler<Notification> handler, UUID site, String engineName)
    {
        return new RabbitConsumer<Notification>(this.broker, this.transcoder.asQueueEventTranscoder(Notification.class), handler, this.source.getRegistry().timer("consume-notifications"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                String queueName = "bergamot.notification." + (site == null ? "default" : site.toString()) + "." + engineName;
                on.queueDeclare(queueName, true, false, false, null);
                on.exchangeDeclare("bergamot.notification", "topic", true);
                on.queueBind(queueName, "bergamot.notification", site == null ? "#" : site.toString());
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}
