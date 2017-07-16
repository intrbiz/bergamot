package com.intrbiz.bergamot.queue.impl.hcq;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.health.HealthCheckMessage;
import com.intrbiz.bergamot.queue.HealthCheckQueue;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.hcq.client.HCQBatch;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.hcq.HCQConsumer;
import com.intrbiz.queue.hcq.HCQPool;
import com.intrbiz.queue.hcq.HCQProducer;
import com.intrbiz.queue.name.NullKey;

public class HCQHealthCheckQueue extends HealthCheckQueue
{
    public static final int QUEUE_SIZE = 100;
    
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(HealthCheckQueue.class, HCQPool.TYPE, HCQHealthCheckQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public HCQHealthCheckQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "control-queue";
    }

    @Override
    public Producer<HealthCheckMessage> publishHealthCheckEvents()
    {
        return new HCQProducer<HealthCheckMessage, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(HealthCheckMessage.class), new NullKey(), this.source.getRegistry().timer("publish-health-checks"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.health.event", "fanout");
                return "bergamot.health.event";
            }
        };
    }
    
    @Override
    public Producer<HealthCheckMessage> publishHealthCheckControlEvents()
    {
        return new HCQProducer<HealthCheckMessage, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(HealthCheckMessage.class), new NullKey(), this.source.getRegistry().timer("publish-health-checks"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.health.control", "fanout");
                return "bergamot.health.control";
            }
        };
    }

    @Override
    public Consumer<HealthCheckMessage, NullKey> consumeHealthCheckEvents(DeliveryHandler<HealthCheckMessage> handler)
    {
        return new HCQConsumer<HealthCheckMessage, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(HealthCheckMessage.class), handler, this.source.getRegistry().timer("consume-health-checks"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                String queueName = "health-" + UUID.randomUUID();
                on.getOrCreateTempQueue(queueName, QUEUE_SIZE)
                 .getOrCreateExchange("bergamot.health.event", "fanout")
                 .bindQueueToExchange("bergamot.health.event", "", queueName);
                return queueName;
            }
        };
    }
    
    @Override
    public Consumer<HealthCheckMessage, NullKey> consumeHealthCheckControlEvents(DeliveryHandler<HealthCheckMessage> handler)
    {
        return new HCQConsumer<HealthCheckMessage, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(HealthCheckMessage.class), handler, this.source.getRegistry().timer("consume-health-checks"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                String queueName = "health-" + UUID.randomUUID(); 
                on.getOrCreateTempQueue(queueName, QUEUE_SIZE)
                 .getOrCreateExchange("bergamot.health.control", "fanout")
                 .bindQueueToExchange("bergamot.health.control", "", queueName);
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}
