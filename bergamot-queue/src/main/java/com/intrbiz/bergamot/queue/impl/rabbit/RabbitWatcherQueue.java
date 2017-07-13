package com.intrbiz.bergamot.queue.impl.rabbit;

import java.io.IOException;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;
import com.intrbiz.bergamot.queue.WatcherQueue;
import com.intrbiz.bergamot.queue.key.WatcherKey;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitPool;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

public class RabbitWatcherQueue extends WatcherQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(WatcherQueue.class, RabbitPool.TYPE, RabbitWatcherQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public RabbitWatcherQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "watcher-queue";
    }

    @Override
    public RoutedProducer<CheckEvent, WatcherKey> publishWatcherEvents()
    {
        return new RabbitProducer<CheckEvent, WatcherKey>(this.broker, this.transcoder.asQueueEventTranscoder(CheckEvent.class), null, this.source.getRegistry().timer("publish-watcher-events"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.watcher", "topic", true);
                return "bergamot.watcher";
            }
        };
    }

    @Override
    public Consumer<CheckEvent, WatcherKey> consumeWatcherEvents(DeliveryHandler<CheckEvent> handler, UUID watcher, String engine)
    {
        return new RabbitConsumer<CheckEvent, WatcherKey>(this.broker, this.transcoder.asQueueEventTranscoder(CheckEvent.class), handler, this.source.getRegistry().timer("consume-watcher-events"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                // the transient watcher queue
                String queueName = "bergamot.queue.watcher." + watcher + "." + engine;
                on.queueDeclare(queueName, false, true, true, null);
                on.exchangeDeclare("bergamot.watcher", "topic", true);
                on.queueBind(queueName, "bergamot.watcher", new WatcherKey(watcher, engine).toString());
                return queueName;
            }
        };
    }

    @Override
    public void close()
    {
    }
}
