package com.intrbiz.bergamot.cluster.broker;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.update.Update;

public final class SiteUpdateBroker extends GenericPartitionedBroker<UUID, Update>
{
    public SiteUpdateBroker(HazelcastInstance hazelcast)
    {
        super(hazelcast, ObjectNames::getSiteNotificationTopicName);
    }
}
