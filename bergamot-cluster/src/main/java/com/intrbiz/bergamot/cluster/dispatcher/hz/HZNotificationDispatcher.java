package com.intrbiz.bergamot.cluster.dispatcher.hz;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
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
        // Offer to the notifier
        return this.offer(notifierId, notification);
    }
    
    protected UUID routeNotification(Notification check)
    {
        return this.notifierRouteTable.route(check.getSite().getId(), check.getEngine());
    }
    
    @Override
    public Set<String> getAvailableEngines(UUID siteId)
    {
        return this.notifierRouteTable.getAvailableEngines(siteId);
    }
}
