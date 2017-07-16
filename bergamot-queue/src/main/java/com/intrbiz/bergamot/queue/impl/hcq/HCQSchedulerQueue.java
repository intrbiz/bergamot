package com.intrbiz.bergamot.queue.impl.hcq;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.queue.SchedulerQueue;
import com.intrbiz.bergamot.queue.key.SchedulerKey;
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

public class HCQSchedulerQueue extends SchedulerQueue
{
    public static final int QUEUE_SIZE = 100;
    
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(SchedulerQueue.class, HCQPool.TYPE, HCQSchedulerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public HCQSchedulerQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "scheduler-queue";
    }

    @Override
    public RoutedProducer<SchedulerAction, SchedulerKey> publishSchedulerActions(SchedulerKey defaultKey)
    {
        return new HCQProducer<SchedulerAction, SchedulerKey>(this.broker, this.transcoder.asQueueEventTranscoder(SchedulerAction.class), defaultKey, this.source.getRegistry().timer("publish-scheduler-action"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.scheduler", "topic");
                return "bergamot.scheduler";
            }
        };
    }

    @Override
    public Consumer<SchedulerAction, SchedulerKey> consumeSchedulerActions(DeliveryHandler<SchedulerAction> handler)
    {
        return new HCQConsumer<SchedulerAction, SchedulerKey>(this.broker, this.transcoder.asQueueEventTranscoder(SchedulerAction.class), handler, this.source.getRegistry().timer("consume-scheduler-action"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.scheduler", "topic");
                String queueName = "bergamot.scheduler." + UUID.randomUUID(); 
                on.getOrCreateTempQueue(queueName, QUEUE_SIZE);
                return queueName;
            }

            @Override
            protected void addQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.bindQueueToExchange("bergamot.scheduler", binding, this.queue);
            }
            
            @Override
            protected void removeQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.unbindQueueToExchange("bergamot.scheduler", binding, this.queue);
            }
        };
    }

    @Override
    public void close()
    {
    }
}
