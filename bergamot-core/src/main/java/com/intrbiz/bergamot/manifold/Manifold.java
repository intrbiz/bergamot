package com.intrbiz.bergamot.manifold;

import java.util.Collection;
import java.util.List;

import com.intrbiz.bergamot.config.ManifoldCfg;
import com.intrbiz.bergamot.engine.BergamotEngine;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.name.Queue;

/**
 * The Manifold routes messages onto various queues
 */
public interface Manifold extends BergamotEngine<ManifoldCfg>
{
    // routing
    
    List<MessageRouter> getRouters();
    
    void addRouter(MessageRouter router);
    
    void clearRouters();
    
    // exchanges
    
    void setupExchange(Exchange exchange, GenericKey defaultKey);
    
    boolean hasExchange(String name);
    
    RoutedProducer<Message, GenericKey> getExchange(String name);
    
    Collection<RoutedProducer<Message, GenericKey>> getExchanges();
    
    // queues / consumers
    
    Consumer<Message, GenericKey> setupConsumer(DeliveryHandler<Message> handler, Exchange exchange, Queue queue, GenericKey... bindings);
    
    Collection<Consumer<Message, GenericKey>> getConsumers();
    
    // publishing
    
    void publish(Message message);
}
