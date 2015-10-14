package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.health.HealthCheckMessage;
import com.intrbiz.bergamot.queue.HealthCheckQueue;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.name.NullKey;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

public class RabbitHealthCheckQueue extends HealthCheckQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(HealthCheckQueue.class, RabbitHealthCheckQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public RabbitHealthCheckQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "control-queue";
    }

    @Override
    public Producer<HealthCheckMessage> publishHealthCheckEvents()
    {
        return new RabbitProducer<HealthCheckMessage, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(HealthCheckMessage.class), new NullKey(), this.source.getRegistry().timer("publish-health-checks"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.health.event", "fanout", true);
                return "bergamot.health.event";
            }
        };
    }
    
    @Override
    public Producer<HealthCheckMessage> publishHealthCheckControlEvents()
    {
        return new RabbitProducer<HealthCheckMessage, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(HealthCheckMessage.class), new NullKey(), this.source.getRegistry().timer("publish-health-checks"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.health.control", "fanout", true);
                return "bergamot.health.control";
            }
        };
    }

    @Override
    public Consumer<HealthCheckMessage, NullKey> consumeHealthCheckEvents(DeliveryHandler<HealthCheckMessage> handler)
    {
        return new RabbitConsumer<HealthCheckMessage, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(HealthCheckMessage.class), handler, this.source.getRegistry().timer("consume-health-checks"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                String queueName = on.queueDeclare().getQueue();
                on.exchangeDeclare("bergamot.health.event", "fanout", true);
                on.queueBind(queueName, "bergamot.health.event", "");
                return queueName;
            }
        };
    }
    
    @Override
    public Consumer<HealthCheckMessage, NullKey> consumeHealthCheckControlEvents(DeliveryHandler<HealthCheckMessage> handler)
    {
        return new RabbitConsumer<HealthCheckMessage, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(HealthCheckMessage.class), handler, this.source.getRegistry().timer("consume-health-checks"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                String queueName = on.queueDeclare().getQueue();
                on.exchangeDeclare("bergamot.health.control", "fanout", true);
                on.queueBind(queueName, "bergamot.health.control", "");
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}
