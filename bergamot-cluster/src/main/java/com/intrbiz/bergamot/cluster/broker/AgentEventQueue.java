package com.intrbiz.bergamot.cluster.broker;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.event.agent.AgentEvent;

public class AgentEventQueue extends GenericGlobalQueue<AgentEvent>
{
    public AgentEventQueue(HazelcastInstance hazelcast)
    {
        super(hazelcast, ObjectNames.getAgentEventQueueName());
    }

}
