package com.intrbiz.bergamot.queue.impl.rabbit;

import java.io.IOException;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.event.control.ControlEvent;
import com.intrbiz.bergamot.queue.ControlQueue;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.name.NullKey;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitPool;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

public class RabbitControlQueue extends ControlQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(ControlQueue.class, RabbitPool.TYPE, RabbitControlQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public RabbitControlQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "control-queue";
    }

    @Override
    public Producer<ControlEvent> publishControlEvents()
    {
        return new RabbitProducer<ControlEvent, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(ControlEvent.class), new NullKey(), this.source.getRegistry().timer("publish-control-events"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.control", "topic", true);
                return "bergamot.control";
            }
        };
    }

    @Override
    public Consumer<ControlEvent, NullKey> consumeControlEvents(DeliveryHandler<ControlEvent> handler)
    {
        return new RabbitConsumer<ControlEvent, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(ControlEvent.class), handler, this.source.getRegistry().timer("consume-control-events"))
        {
            public String setupQueue(Channel on) throws IOException
            {
                on.queueDeclare("bergamot.control.queue", true, false, false, null);
                on.exchangeDeclare("bergamot.control", "topic", true);
                on.queueBind("bergamot.control.queue", "bergamot.control", "#");
                return "bergamot.control.queue";
            }
        };
    }

    @Override
    public void close()
    {
    }
}
