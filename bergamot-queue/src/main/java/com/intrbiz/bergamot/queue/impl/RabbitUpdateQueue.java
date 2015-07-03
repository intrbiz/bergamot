package com.intrbiz.bergamot.queue.impl;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.update.Update;
import com.intrbiz.bergamot.queue.UpdateQueue;
import com.intrbiz.bergamot.queue.key.UpdateKey;
import com.intrbiz.gerald.source.IntelligenceSource;
import com.intrbiz.gerald.witchcraft.Witchcraft;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

public class RabbitUpdateQueue extends UpdateQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(UpdateQueue.class, RabbitUpdateQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;
    
    private final IntelligenceSource source = Witchcraft.get().source("com.intrbiz.bergamot.queue");

    @SuppressWarnings("unchecked")
    public RabbitUpdateQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "notification-queue";
    }

    @Override
    public RoutedProducer<Update, UpdateKey> publishUpdates(UpdateKey defaultKey)
    {
        return new RabbitProducer<Update, UpdateKey>(this.broker, this.transcoder.asQueueEventTranscoder(Update.class), defaultKey, this.source.getRegistry().timer("publish-update"))
        {
            protected String setupExchange(Channel on) throws IOException
            {
                String name = "bergamot.update"; 
                on.exchangeDeclare(name, "topic", true);
                return name;
            }
        };
    }

    @Override
    public Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler, UUID site, UUID check)
    {
        return new RabbitConsumer<Update, UpdateKey>(this.broker, this.transcoder.asQueueEventTranscoder(Update.class), handler, this.source.getRegistry().timer("consume-update"))
        {
            protected String setupQueue(Channel on) throws IOException
            {
                String queueName = on.queueDeclare().getQueue();
                on.exchangeDeclare("bergamot.update", "topic", true);
                on.queueBind(queueName, "bergamot.update", (site == null ? "*" : site.toString()) + "." + (check == null ? "*" : check.toString()));
                return queueName;
            }
            
            protected void addQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueBind(this.queue, "bergamot.update", binding);
            }
        };
    }
    
    @Override
    public Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler)
    {
        return new RabbitConsumer<Update, UpdateKey>(this.broker, this.transcoder.asQueueEventTranscoder(Update.class), handler, this.source.getRegistry().timer("consume-update"))
        {
            protected String setupQueue(Channel on) throws IOException
            {
                String queueName = on.queueDeclare().getQueue();
                on.exchangeDeclare("bergamot.update", "topic", true);
                // no bindings for the moment
                return queueName;
            }
            
            protected void addQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueBind(this.queue, "bergamot.update", binding);
            }
        };
    }
    
    @Override
    public Consumer<Update, UpdateKey> consumeUpdates(DeliveryHandler<Update> handler, Set<UpdateKey> initialBindings)
    {
        return new RabbitConsumer<Update, UpdateKey>(this.broker, this.transcoder.asQueueEventTranscoder(Update.class), handler, this.source.getRegistry().timer("consume-update"))
        {
            protected String setupQueue(Channel on) throws IOException
            {
                // setup the queue
                String queueName = on.queueDeclare().getQueue();
                on.exchangeDeclare("bergamot.update", "topic", true);
                // bind
                for (UpdateKey binding : initialBindings)
                {
                    on.queueBind(queueName, "bergamot.update", binding.toString());    
                }
                return queueName;
            }
            
            protected void addQueueBinding(Channel on, String binding) throws IOException
            {
                on.queueBind(this.queue, "bergamot.update", binding);
            }
        };
    }

    @Override
    public void close()
    {
    }
}
