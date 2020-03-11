package com.intrbiz.bergamot.cluster.broker;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.event.agent.AgentEvent;

public final class AgentEventBroker extends GenericPartitionedBroker<UUID, AgentEvent>
{
    public AgentEventBroker(HazelcastInstance hazelcast)
    {
        super(hazelcast, ObjectNames::getSiteNotificationTopicName);
    }
}
