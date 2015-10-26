package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.intrbiz.bergamot.accounting.io.BergamotAccountingTranscoder;
import com.intrbiz.bergamot.accounting.model.BergamotAccountingEvent;
import com.intrbiz.bergamot.queue.AccountingQueue;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.Producer;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueException;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.name.NullKey;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

public class RabbitAccountingQueue extends AccountingQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(AccountingQueue.class, RabbitAccountingQueue::new);
    }

    private final BergamotAccountingTranscoder transcoder = new BergamotAccountingTranscoder();

    private final QueueBrokerPool<Channel> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public RabbitAccountingQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
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
        return new RabbitProducer<BergamotAccountingEvent, NullKey>(this.broker, this.queueTranscoder(), new NullKey(), this.source.getRegistry().timer("publish-accounting"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                on.exchangeDeclare("bergamot.accounting", "topic", true);
                return "bergamot.accounting";
            }
        };
    }

    @Override
    public Consumer<BergamotAccountingEvent, NullKey> consumeAccountingEvents(DeliveryHandler<BergamotAccountingEvent> handler)
    {
        return new RabbitConsumer<BergamotAccountingEvent, NullKey>(this.broker, this.queueTranscoder(), handler, this.source.getRegistry().timer("consume-accounting"))
        {
            protected String setupQueue(Channel on) throws IOException
            {
                on.queueDeclare("bergamot.accounting", true, false, false, null);
                on.exchangeDeclare("bergamot.accounting", "topic", true);
                on.queueBind("bergamot.accounting", "bergamot.accounting", "#");
                return "bergamot.accounting";
            }
        };
    }

    @Override
    public void close()
    {
    }
}
