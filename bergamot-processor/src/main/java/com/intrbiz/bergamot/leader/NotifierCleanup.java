package com.intrbiz.bergamot.leader;

import java.util.UUID;
import java.util.function.Function;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistry;
import com.intrbiz.bergamot.cluster.registry.model.RegistryEvent.Type;

public class NotifierCleanup
{
    private static final Logger logger = Logger.getLogger(NotifierCleanup.class);
    
    private final NotifierRegistry notifierRegistry;
    
    private final Function<UUID, NotificationConsumer> notificationConsumerFactory;
    
    private final NotificationDispatcher notificationDispatcher;
    
    private UUID listenerId;

    public NotifierCleanup(NotifierRegistry notifierRegistry, Function<UUID, NotificationConsumer> notificationConsumerFactory, NotificationDispatcher notificationDispatcher)
    {
        super();
        this.notifierRegistry = notifierRegistry;
        this.notificationConsumerFactory = notificationConsumerFactory;
        this.notificationDispatcher = notificationDispatcher;
    }
    
    public void start()
    {
        this.listenerId = this.notifierRegistry.listen((event) -> {
            if (event.getType() == Type.REMOVED)
            {
                this.cleanUpNotifier(event.getId());
            }
        });
    }
    
    protected void cleanUpNotifier(UUID notifier)
    {
        logger.info("Cleaning up notifier queue " + notifier);
        NotificationConsumer consumer = this.notificationConsumerFactory.apply(notifier);
        // Drain down the worker queue
        consumer.drainTo(this.notificationDispatcher::dispatchNotification);
        // Destroy the worker queue
        consumer.destroy();
    }
    
    public void stop()
    {
        if (this.listenerId != null)
        {
            this.notifierRegistry.unlisten(this.listenerId);
            this.listenerId = null;
        }
    }
}
