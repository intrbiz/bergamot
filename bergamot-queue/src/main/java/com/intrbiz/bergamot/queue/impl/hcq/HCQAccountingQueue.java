package com.intrbiz.bergamot.queue.impl.hcq;

import java.nio.ByteBuffer;

import com.intrbiz.bergamot.accounting.io.BergamotAccountingTranscoder;
import com.intrbiz.bergamot.accounting.model.BergamotAccountingEvent;
import com.intrbiz.bergamot.queue.AccountingQueue;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.hcq.client.HCQBatch;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.hcq.HCQConsumer;
import com.intrbiz.queue.hcq.HCQPool;
import com.intrbiz.queue.hcq.HCQProducer;
import com.intrbiz.queue.name.NullKey;

public class HCQAccountingQueue extends AccountingQueue
{
    public static final int QUEUE_SIZE = 1000;
    
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(AccountingQueue.class, HCQPool.TYPE, HCQAccountingQueue::new);
    }
    
    public static final String ACCOUNTING_QUEUE_NAME = "bergamot.queue.accounting";
    
    public static final String ACCOUNTING_EXCHANGE_NAME = "bergamot.accounting";

    private final BergamotAccountingTranscoder transcoder = new BergamotAccountingTranscoder();

    private final QueueBrokerPool<HCQClient> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public HCQAccountingQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "accounting-queue";
    }
    
    private QueueEventTranscoder<BergamotAccountingEvent> queueTranscoder()
    {
        return new QueueEventTranscoder<BergamotAccountingEvent>()
        {
            @Override
            public byte[] encodeAsBytes(BergamotAccountingEvent event) throws QueueException
            {
                byte[] body = new byte[1024];
                transcoder.encode(event, ByteBuffer.wrap(body));
                return body;
            }

            @Override
            public BergamotAccountingEvent decodeFromBytes(byte[] data) throws QueueException
            {
                return transcoder.decode(ByteBuffer.wrap(data));
            }
        };
    }

    @Override
    public Producer<BergamotAccountingEvent> publishAccountingEvents()
    {
        return new HCQProducer<BergamotAccountingEvent, NullKey>(this.broker, this.queueTranscoder(), new NullKey(), this.source.getRegistry().timer("publish-accounting"))
        {
            protected String setupExchange(HCQBatch on) throws Exception
            {
                on.getOrCreateExchange(ACCOUNTING_EXCHANGE_NAME, "topic");
                return ACCOUNTING_EXCHANGE_NAME;
            }
        };
    }

    @Override
    public Consumer<BergamotAccountingEvent, NullKey> consumeAccountingEvents(DeliveryHandler<BergamotAccountingEvent> handler)
    {
        return new HCQConsumer<BergamotAccountingEvent, NullKey>(this.broker, this.queueTranscoder(), handler, this.source.getRegistry().timer("consume-accounting"))
        {
            protected String setupQueue(HCQBatch on) throws Exception
            {
                on
                 .getOrCreateQueue(ACCOUNTING_QUEUE_NAME, QUEUE_SIZE, false)
                 .getOrCreateExchange(ACCOUNTING_EXCHANGE_NAME, "topic")
                 .bindQueueToExchange(ACCOUNTING_EXCHANGE_NAME, "#", ACCOUNTING_QUEUE_NAME);
                return ACCOUNTING_QUEUE_NAME;
            }
        };
    }

    @Override
    public void close()
    {
    }
}
