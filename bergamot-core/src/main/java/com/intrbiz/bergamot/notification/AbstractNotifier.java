package com.intrbiz.bergamot.notification;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.component.AbstractComponent;
import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.config.NotifierCfg;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.GenericKey;
import com.intrbiz.queue.name.Queue;

public abstract class AbstractNotifier extends AbstractComponent<NotifierCfg> implements Notifier, DeliveryHandler<Message>
{
    private Logger logger = Logger.getLogger(AbstractNotifier.class);

    protected Map<String, NotificationEngine> engines = new TreeMap<String, NotificationEngine>();

    protected List<Consumer<Message, GenericKey>> consumers = new LinkedList<Consumer<Message, GenericKey>>();

    public AbstractNotifier()
    {
        super();
    }

    @Override
    protected void configure() throws Exception
    {
        // load the notification engines
        for (NotificationEngineCfg engineCfg : this.config.getEngines())
        {
            NotificationEngine engine = (NotificationEngine) engineCfg.create();
            engine.setNotifier(this);
            logger.info("Adding notification engine: " + engine);
            this.engines.put(engine.getName(), engine);
        }
    }

    @Override
    public List<NotificationEngine> getEngines()
    {
        return new LinkedList<NotificationEngine>(this.engines.values());
    }

    @Override
    public void start() throws Exception
    {
        // start all the consumers
        for (int i = 0; i < this.config.getThreads(); i++)
        {
            logger.info("Creating consumer " + i);
            Exchange exchange = this.config.getExchange().asExchange();
            Queue queue = this.config.getQueue() == null ? null : this.config.getQueue().asQueue();
            GenericKey[] bindings = this.config.asBindings();
            Consumer<Message, GenericKey> consumer = this.getBergamot().getManifold().setupConsumer(this, exchange, queue, bindings);
            this.consumers.add(consumer);
        }
    }

    protected void sendNotification(Notification notification)
    {
        for (NotificationEngine notificationEngine : this.engines.values())
        {
            notificationEngine.sendNotification(notification);
        }
    }

    @Override
    public void handleDevliery(Message event) throws IOException
    {
        if (event instanceof Notification)
        {
            logger.debug("Got notification: " + event);
            this.sendNotification((Notification) event);
        }
        else
        {
            logger.warn("Got non-notification message, ignoring: " + event);
        }
    }
}
