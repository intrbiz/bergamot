package com.intrbiz.bergamot.queue.impl.hcq;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.event.control.ControlEvent;
import com.intrbiz.bergamot.queue.ControlQueue;
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

public class HCQControlQueue extends ControlQueue
{
    public static final int QUEUE_SIZE = 100;
    
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(ControlQueue.class, HCQPool.TYPE, HCQControlQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public HCQControlQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "control-queue";
    }

    @Override
    public Producer<ControlEvent> publishControlEvents()
    {
        return new HCQProducer<ControlEvent, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(ControlEvent.class), new NullKey(), this.source.getRegistry().timer("publish-control-events"))
        {
            protected String setupExchange(HCQBatch on) throws Exception
            {
                on.getOrCreateExchange("bergamot.control", "topic");
                return "bergamot.control";
            }
        };
    }

    @Override
    public Consumer<ControlEvent, NullKey> consumeControlEvents(DeliveryHandler<ControlEvent> handler)
    {
        return new HCQConsumer<ControlEvent, NullKey>(this.broker, this.transcoder.asQueueEventTranscoder(ControlEvent.class), handler, this.source.getRegistry().timer("consume-control-events"))
        {
            public String setupQueue(HCQBatch on) throws Exception
            {
                on
                 .getOrCreateQueue("bergamot.control.queue", QUEUE_SIZE, false)
                 .getOrCreateExchange("bergamot.control", "topic")
                 .bindQueueToExchange("bergamot.control", "#", "bergamot.control.queue");
                return "bergamot.control.queue";
            }
        };
    }

    @Override
    public void close()
    {
    }
}
