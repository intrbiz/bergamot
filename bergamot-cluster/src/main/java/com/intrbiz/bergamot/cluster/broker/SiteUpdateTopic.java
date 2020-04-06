package com.intrbiz.bergamot.cluster.broker;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.event.update.Update;

public final class SiteUpdateTopic extends GenericPartitionedTopic<UUID, Update>
{
    public SiteUpdateTopic(HazelcastInstance hazelcast)
    {
        super(hazelcast, HZNames::getSiteNotificationTopicName);
    }
}
