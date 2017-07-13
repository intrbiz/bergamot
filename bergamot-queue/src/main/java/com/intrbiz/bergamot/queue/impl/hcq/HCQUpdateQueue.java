package com.intrbiz.bergamot.queue.impl.hcq;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.UpdateQueue;
import com.intrbiz.bergamot.queue.key.UpdateKey;
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

public class HCQUpdateQueue extends UpdateQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(UpdateQueue.class, HCQPool.TYPE, HCQUpdateQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public HCQUpdateQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "update-queue";
    }

    @Override
    public RoutedProducer<Update, UpdateKey> publishUpdates(UpdateKey defaultKey)
    {
        return new HCQProducer<Update, UpdateKey>(this.broker, this.transcoder.asQueueEventTranscoder(Update.class), defaultKey, this.source.getRegistry().timer("publish-update"))
        {
            protected String setupExchange(HCQBatch on) throws IOException
            {
                String name = "bergamot.update"; 
                on.getOrCreateExchange(name, "topic");
                return name;
            }
        };
    }

    @Override
    public Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler, UUID site, UUID check)
    {
        return new HCQConsumer<Update, UpdateKey>(this.broker, this.transcoder.asQueueEventTranscoder(Update.class), handler, this.source.getRegistry().timer("consume-update"))
        {
            protected String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.update", "topic");
                String queueName = "update-" + UUID.randomUUID();
                on.getOrCreateTempQueue(queueName);
                on.bindQueueToExchange("bergamot.update", (site == null ? "*" : site.toString()) + "." + (check == null ? "*" : check.toString()), queueName);
                return queueName;
            }
            
            protected void addQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.bindQueueToExchange("bergamot.update", binding, this.queue);
            }
            
            protected void removeQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.unbindQueueToExchange("bergamot.update", binding, this.queue);
            }
        };
    }
    
    @Override
    public Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler)
    {
        return new HCQConsumer<Update, UpdateKey>(this.broker, this.transcoder.asQueueEventTranscoder(Update.class), handler, this.source.getRegistry().timer("consume-update"))
        {
            protected String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.update", "topic");
                String queueName = "update-" + UUID.randomUUID();
                on.getOrCreateTempQueue(queueName);
                // no bindings for the moment
                return queueName;
            }
            
            protected void addQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.bindQueueToExchange("bergamot.update", binding, this.queue);
            }
            
            protected void removeQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.unbindQueueToExchange("bergamot.update", binding, this.queue);
            }
        };
    }
    
    @Override
    public Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler, Set<UpdateKey> initialBindings)
    {
        return new HCQConsumer<Update, UpdateKey>(this.broker, this.transcoder.asQueueEventTranscoder(Update.class), handler, this.source.getRegistry().timer("consume-update"))
        {
            protected String setupQueue(HCQBatch on) throws IOException
            {
                on.getOrCreateExchange("bergamot.update", "topic");
                // setup the queue
                String queueName = "update-" + UUID.randomUUID();
                on.getOrCreateTempQueue(queueName);
                // bind
                for (UpdateKey binding : initialBindings)
                {
                    on.bindQueueToExchange("bergamot.update", binding.toString(), queueName);    
                }
                return queueName;
            }
            
            protected void addQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.bindQueueToExchange("bergamot.update", binding, this.queue);
            }
            
            protected void removeQueueBinding(HCQBatch on, String binding) throws IOException
            {
                on.unbindQueueToExchange("bergamot.update", binding, this.queue);
            }
        };
    }

    @Override
    public void close()
    {
    }
}
