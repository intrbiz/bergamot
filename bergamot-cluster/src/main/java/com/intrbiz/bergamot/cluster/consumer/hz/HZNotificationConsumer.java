package com.intrbiz.bergamot.cluster.consumer.hz;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.notification.Notification;

public class HZNotificationConsumer extends HZBaseConsumer<Notification> implements NotificationConsumer
{    
    public HZNotificationConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super(hazelcast, id, HZNames::buildNotifierRingbufferName, HZNames::buildNotifiersSequenceMapName);
    }
}
