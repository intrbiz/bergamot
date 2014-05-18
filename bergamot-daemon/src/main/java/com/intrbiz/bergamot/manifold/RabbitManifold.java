package com.intrbiz.bergamot.manifold;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueEventTranscoder;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.rabbit.RabbitConsumer;
import com.intrbiz.queue.rabbit.RabbitProducer;
import com.rabbitmq.client.Channel;

/**
 * Use RabbitMQ as the queue implementation
 */
public class RabbitManifold extends AbstractManifold
{
    public RabbitManifold()
    {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RoutedProducer<Message, GenericKey> createProducer(Exchange exchange, GenericKey defaultRoutingKey)
    {
        QueueBrokerPool<Channel> broker = (QueueBrokerPool<Channel>) QueueManager.getInstance().defaultBroker();
        QueueEventTranscoder<Message> queueTranscoder = new BergamotTranscoder().asQueueEventTranscoder(Message.class);
        return new RabbitProducer<Message, GenericKey>(broker, queueTranscoder, exchange, defaultRoutingKey);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Consumer<Message, GenericKey> createConsumer(DeliveryHandler<Message> handler, Exchange exchange, Queue queue, GenericKey... bindings)
    {
        QueueBrokerPool<Channel> broker = (QueueBrokerPool<Channel>) QueueManager.getInstance().defaultBroker();
        QueueEventTranscoder<Message> queueTranscoder = new BergamotTranscoder().asQueueEventTranscoder(Message.class);
        return new RabbitConsumer<Message, GenericKey>(broker, queueTranscoder, handler, queue, exchange, bindings);
    }
}
