package com.intrbiz.bergamot.notification;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.config.NotificationEngineCfg;
import com.intrbiz.bergamot.config.NotifierCfg;
import com.intrbiz.bergamot.model.message.notification.Notification;
import com.intrbiz.bergamot.queue.NotificationQueue;
import com.intrbiz.bergamot.queue.key.NotificationKey;
import com.intrbiz.queue.Consumer;

public abstract class AbstractNotifier implements Notifier
{
    private Logger logger = Logger.getLogger(AbstractNotifier.class);

    protected Map<String, NotificationEngine> engines = new TreeMap<String, NotificationEngine>();
    
    protected NotificationQueue queue;

    protected List<Consumer<Notification, NotificationKey>> consumers = new LinkedList<Consumer<Notification, NotificationKey>>();

    protected NotifierCfg configuration;
    
    protected UUID site = null;
    
    protected long sleepTime = 0;

    public AbstractNotifier()
    {
        super();
    }
    
    protected abstract String getNotifierName();

    @Override
    public final void configure(NotifierCfg cfg) throws Exception
    {
        this.configuration = cfg;
        this.configure();
    }

    @Override
    public final NotifierCfg getConfiguration()
    {
        return this.configuration;
    }

    protected void configure() throws Exception
    {
        this.site = this.configuration.getSite();
        this.sleepTime = this.configuration.getSleepTime() <= 0 ? 0 : TimeUnit.SECONDS.toMillis(this.configuration.getSleepTime());
        logger.info("Using " + this.configuration.getThreads() + " thread with sleep time of " + this.sleepTime);
        // load the notification engines
        for (NotificationEngineCfg engineCfg : this.configuration.getEngines())
        {
            NotificationEngine engine = (NotificationEngine) engineCfg.create();
            engine.setNotifier(this);
            logger.info("Adding notification engine: " + engine);
            this.engines.put(engine.getName(), engine);
        }
    }
    
    @Override
    public UUID getSite()
    {
        return this.site;
    }
    
    public long getSleepTime()
    {
        return this.sleepTime;
    }

    @Override
    public List<NotificationEngine> getEngines()
    {
        return new LinkedList<NotificationEngine>(this.engines.values());
    }

    @Override
    public void start() throws Exception
    {
        // open the queue
        this.queue = NotificationQueue.open();
        // start all the consumers
        for (int i = 0; i < this.configuration.getThreads(); i++)
        {
            logger.info("Creating consumer " + i);
            this.consumers.add(this.queue.consumeNotifications((h, n) -> this.sendNotification(n), this.getSite(), this.getNotifierName()));
        }
    }

    @Override
    public void sendNotification(Notification notification)
    {
        // send
        for (NotificationEngine notificationEngine : this.engines.values())
        {
            notificationEngine.sendNotification(notification);
        }
        sleep();
    }
    
    protected void sleep()
    {
        // sleep, an attempt to avoid mail services banning 
        // an account
        // TODO: improve
        if (this.getSleepTime() > 0)
        {
            try
            {
                Thread.sleep(this.getSleepTime());
            }
            catch (InterruptedException e)
            {
            }
        }
    }
    
    public abstract String getDaemonName();
}
