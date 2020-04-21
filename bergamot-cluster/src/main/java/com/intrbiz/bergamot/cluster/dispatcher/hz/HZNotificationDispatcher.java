package com.intrbiz.bergamot.cluster.dispatcher.hz;

import java.util.Objects;
import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.ringbuffer.Ringbuffer;
import com.intrbiz.bergamot.cluster.dispatcher.NotificationDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistry;
import com.intrbiz.bergamot.cluster.registry.NotifierRouteTable;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.notification.Notification;

/**
 * Dispatch notifications to notifiers
 */
public class HZNotificationDispatcher extends HZBaseDispatcher<Notification> implements NotificationDispatcher
{
    private final NotifierRegistry notifiers;
    
    private final NotifierRouteTable notifierRouteTable;
    
    public HZNotificationDispatcher(NotifierRegistry notifiers, HazelcastInstance hazelcast)
    {
        super(hazelcast, HZNames::buildNotifierRingbufferName);
        this.notifiers = Objects.requireNonNull(notifiers);
        this.notifierRouteTable = Objects.requireNonNull(this.notifiers.getRouteTable());
    }
    
    public PublishStatus dispatchNotification(Notification notification)
    {
        // Pick a notifier for this check
        UUID notifierId = this.routeNotification(notification);
        if (notifierId == null) return PublishStatus.Unroutable;
        // Get the notifier queue
        // TODO: maybe we should do an async offer
        Ringbuffer<Notification> queue = this.getRingbuffer(notifierId);
        queue.add(notification);
        return PublishStatus.Success;
    }
    
    protected UUID routeNotification(Notification check)
    {
        return this.notifierRouteTable.route(check.getSite().getId(), check.getEngine());
    }
}
