package com.intrbiz.bergamot.cluster.dispatcher;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.collection.IQueue;
import com.hazelcast.collection.impl.queue.QueueService;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistry;
import com.intrbiz.bergamot.cluster.registry.NotifierRouteTable;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.notification.Notification;

/**
 * Dispatch notifications to notifiers
 */
public class NotificationDispatcher
{
    private final NotifierRegistry notifiers;
    
    private final NotifierRouteTable notifierRouteTable;
    
    private final HazelcastInstance hazelcast;
    
    private final ConcurrentMap<String, IQueue<Notification>> queuesCache = new ConcurrentHashMap<>();
    
    public NotificationDispatcher(NotifierRegistry notifiers, HazelcastInstance hazelcast)
    {
        super();
        this.notifiers = Objects.requireNonNull(notifiers);
        this.notifierRouteTable = Objects.requireNonNull(this.notifiers.getRouteTable());
        this.hazelcast = Objects.requireNonNull(hazelcast);
        // Setup a object listener to clean up our queue cache
        this.hazelcast.addDistributedObjectListener(new QueueListener());
    }
    
    public PublishStatus dispatchNotification(Notification notification)
    {
        // Pick a notifier for this check
        UUID workerId = this.routeNotification(notification);
        if (workerId == null) return PublishStatus.Unroutable;
        // Get the notifier queue
        IQueue<Notification> queue = this.getNotifierQueue(workerId);
        // Offer onto the notifier queue
        boolean success = queue.offer(notification);
        return success ? PublishStatus.Success : PublishStatus.Failed;

    }
    
    protected UUID routeNotification(Notification check)
    {
        return this.notifierRouteTable.route(check.getSite().getId(), check.getEngine());
    }
    
    private IQueue<Notification> getNotifierQueue(UUID workerId)
    {
        return this.queuesCache.computeIfAbsent(HZNames.buildNotifierQueueName(workerId), this.hazelcast::getQueue);
    }

    private class QueueListener implements DistributedObjectListener
    {
        @Override
        public void distributedObjectCreated(DistributedObjectEvent event)
        {
        }

        @Override
        public void distributedObjectDestroyed(DistributedObjectEvent event)
        {
            if (QueueService.SERVICE_NAME.equals(event.getServiceName()))
            {
                NotificationDispatcher.this.queuesCache.remove(event.getObjectName());
            }
        }
    }
}
