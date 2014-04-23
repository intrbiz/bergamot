package com.intrbiz.bergamot.manifold;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.component.AbstractComponent;
import com.intrbiz.bergamot.config.ExchangeCfg;
import com.intrbiz.bergamot.config.ManifoldCfg;
import com.intrbiz.bergamot.config.RouterCfg;
import com.intrbiz.bergamot.manifold.model.MessageContext;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.name.Queue;

public abstract class AbstractManifold extends AbstractComponent<ManifoldCfg> implements Manifold
{
    private Logger logger = Logger.getLogger(AbstractManifold.class);

    protected List<MessageRouter> routers = new LinkedList<MessageRouter>();

    protected Map<String, RoutedProducer<Message, GenericKey>> exchanges = new TreeMap<String, RoutedProducer<Message, GenericKey>>();
    
    protected List<Consumer<Message, GenericKey>> consumers = new LinkedList<Consumer<Message, GenericKey>>();

    public AbstractManifold()
    {
        super();
    }

    public List<MessageRouter> getRouters()
    {
        return this.routers;
    }

    public void addRouter(MessageRouter router)
    {
        this.routers.add(router);
    }

    public void clearRouters()
    {
        this.routers.clear();
    }

    protected void applyRouting(MessageContext task)
    {
        for (MessageRouter router : routers)
        {
            if (router.route(task)) return;
        }
        throw new RuntimeException("Failed to apply routing, no router for task: " + task.getClass().getSimpleName());
    }

    public void publish(Message message)
    {
        try
        {
            MessageContext ctx = new MessageContext(message);
            // stamp in some information
            ctx.getMessage().setSender(this.getBergamot().getName());
            // route the task
            this.applyRouting(ctx);
            // publish the routed task
            RoutedProducer<Message, GenericKey> producer = this.exchanges.get(ctx.getRouting().getExchange());
            if (producer != null)
            {
                if (ctx.getRouting().getRoutingKey() == null)
                    producer.publish(ctx.getMessage());
                else
                    producer.publish(ctx.getRouting().getRoutingKey(), ctx.getMessage());
            }
            else
            {
                throw new RuntimeException("The exchange " + ctx.getRouting().getExchange() + " has not been setup!");
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to execute task", e);
        }
    }

    @Override
    protected void configure() throws Exception
    {
        // load the routers
        for (RouterCfg routerCfg : this.config.getRouters())
        {
            this.addRouter((MessageRouter) routerCfg.create());
        }
        // load the exchanges
        for (ExchangeCfg exchangeCfg : this.config.getExchanges())
        {
            this.setupExchange(exchangeCfg.asExchange(), exchangeCfg.asKey());
        }
    }

    @Override
    public void setupExchange(Exchange exchange, GenericKey defaultKey)
    {
        // setup the exchange
        RoutedProducer<Message, GenericKey> producer = this.createProducer(exchange, defaultKey);
        this.exchanges.put(exchange.getName(), producer);
    }

    @Override
    public boolean hasExchange(String name)
    {
        return this.exchanges.containsKey(name);
    }

    @Override
    public RoutedProducer<Message, GenericKey> getExchange(String name)
    {
        return this.exchanges.get(name);
    }

    @Override
    public Collection<RoutedProducer<Message, GenericKey>> getExchanges()
    {
        return this.exchanges.values();
    }

    @Override
    public Consumer<Message, GenericKey> setupConsumer(DeliveryHandler<Message> handler, Exchange exchange, Queue queue, GenericKey... bindings)
    {
        Consumer<Message, GenericKey> consumer = this.createConsumer(handler, exchange, queue, bindings);
        this.consumers.add(consumer);
        return consumer;
    }

    @Override
    public Collection<Consumer<Message, GenericKey>> getConsumers()
    {
        return this.consumers;
    }

    protected abstract RoutedProducer<Message, GenericKey> createProducer(Exchange exchange, GenericKey defaultRoutingKey);
    
    protected abstract Consumer<Message, GenericKey> createConsumer(DeliveryHandler<Message> handler, Exchange exchange, Queue queue, GenericKey... bindings);
}
