package com.intrbiz.bergamot.queue.impl.hcq;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.queue.WatcherQueue;
import com.intrbiz.bergamot.queue.key.WatcherKey;
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

public class HCQWatcherQueue extends WatcherQueue
{
    public static final int QUEUE_SIZE = 100;
    
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(WatcherQueue.class, HCQPool.TYPE, HCQWatcherQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public HCQWatcherQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "watcher-queue";
    }

    @Override
    public RoutedProducer<CheckEvent, WatcherKey> publishWatcherEvents()
    {
        return new HCQProducer<CheckEvent, WatcherKey>(this.broker, this.transcoder.asQueueEventTranscoder(CheckEvent.class), null, this.source.getRegistry().timer("publish-watcher-events"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.watcher", "topic");
                return "bergamot.watcher";
            }
        };
    }

    @Override
    public Consumer<CheckEvent, WatcherKey> consumeWatcherEvents(DeliveryHandler<CheckEvent> handler, UUID watcher, String engine)
    {
        return new HCQConsumer<CheckEvent, WatcherKey>(this.broker, this.transcoder.asQueueEventTranscoder(CheckEvent.class), handler, this.source.getRegistry().timer("consume-watcher-events"))
        {
            public String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.watcher", "topic");
                // the transient watcher queue
                String queueName = "watcher-" + UUID.randomUUID();
                on.getOrCreateTempQueue(queueName, QUEUE_SIZE);
                on.bindQueueToExchange("bergamot.watcher", new WatcherKey(watcher, engine).toString(), queueName);
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}
