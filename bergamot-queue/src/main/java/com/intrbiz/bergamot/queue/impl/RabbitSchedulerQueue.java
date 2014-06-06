package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.queue.SchedulerQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

public class RabbitSchedulerQueue extends SchedulerQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(SchedulerQueue.class, RabbitSchedulerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;

    public RabbitSchedulerQueue(QueueBrokerPool<Channel> broker)
    {
        this.broker = broker;
    }

    public String getName()
    {
        return "scheduler-queue";
    }

    @Override
    public RoutedProducer<SchedulerAction> publishSchedulerActions(GenericKey defaultKey)
    {
        return new RabbitProducer<SchedulerAction>(this.broker, this.transcoder.asQueueEventTranscoder(SchedulerAction.class), defaultKey)
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.scheduler", "topic", true);
                return "bergamot.scheduler";
            }
        };
    }

    @Override
    public Consumer<SchedulerAction> consumeSchedulerActions(DeliveryHandler<SchedulerAction> handler, UUID site)
    {
        return new RabbitConsumer<SchedulerAction>(this.broker, this.transcoder.asQueueEventTranscoder(SchedulerAction.class), handler)
        {
            public String setupQueue(Channel on) throws IOException
            {
                String queueName = "bergamot.scheduler." + (site == null ? "default" : site.toString());
                on.queueDeclare(queueName, true, false, false, null);
                on.exchangeDeclare("bergamot.scheduler", "topic", true);
                on.queueBind(queueName, "bergamot.scheduler", site == null ? "#" : site.toString());
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}
