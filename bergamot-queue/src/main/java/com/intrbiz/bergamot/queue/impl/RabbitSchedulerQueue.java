package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.queue.SchedulerQueue;
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

public class RabbitSchedulerQueue extends SchedulerQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(SchedulerQueue.class, RabbitSchedulerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

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
        return new RabbitProducer<SchedulerAction>(this.broker, this.transcoder.asQueueEventTranscoder(SchedulerAction.class), defaultKey, this.source.getRegistry().timer("publish-scheduler-action"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.scheduler", "topic", true);
                return "bergamot.scheduler";
            }
        };
    }

    @Override
    public Consumer<SchedulerAction> consumeSchedulerActions(DeliveryHandler<SchedulerAction> handler)
    {
        return new RabbitConsumer<SchedulerAction>(this.broker, this.transcoder.asQueueEventTranscoder(SchedulerAction.class), handler, this.source.getRegistry().timer("consume-scheduler-action"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                // as scheduling services can move at any time, we need 
                // to use a transient queue
                String queueName = "bergamot.scheduler." + UUID.randomUUID(); 
                on.queueDeclare(queueName, false, true, true, null);
                on.exchangeDeclare("bergamot.scheduler", "topic", true);
                return queueName;
            }

            @Override
            protected void addQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueBind(this.queue, "bergamot.scheduler", binding.toString());
            }
            
            @Override
            protected void removeQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueUnbind(this.queue, "bergamot.scheduler", binding.toString());
            }
        };
    }

    @Override
    public void close()
    {
    }
}
