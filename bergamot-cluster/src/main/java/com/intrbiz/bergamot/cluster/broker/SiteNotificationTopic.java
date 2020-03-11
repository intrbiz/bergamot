package com.intrbiz.bergamot.cluster.broker;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.notification.Notification;

public final class SiteNotificationTopic extends GenericPartitionedTopic<UUID, Notification>
{
    public SiteNotificationTopic(HazelcastInstance hazelcast)
    {
        super(hazelcast, ObjectNames::getSiteNotificationTopicName);
    }
}
