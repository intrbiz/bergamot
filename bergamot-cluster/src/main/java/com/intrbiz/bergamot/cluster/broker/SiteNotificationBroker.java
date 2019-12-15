package com.intrbiz.bergamot.cluster.broker;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.notification.Notification;

public final class SiteNotificationBroker extends GenericPartitionedBroker<UUID, Notification>
{
    public SiteNotificationBroker(HazelcastInstance hazelcast)
    {
        super(hazelcast, ObjectNames::getSiteNotificationTopicName);
    }
}
