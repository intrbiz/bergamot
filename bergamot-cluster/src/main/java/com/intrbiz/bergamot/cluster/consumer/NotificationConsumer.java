package com.intrbiz.bergamot.cluster.consumer;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.notification.Notification;

public class NotificationConsumer extends BaseConsumer<Notification>
{    
    public NotificationConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super(hazelcast, id, HZNames::buildNotifierRingbufferName, HZNames::buildNotifiersSequenceMapName);
    }
}
