package com.intrbiz.bergamot.cluster.broker;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerMessage;

/**
 * Broadcast feed of scheduling changes.
 */
public class SchedulingTopic extends GenericGlobalTopic<SchedulerMessage>
{
    public SchedulingTopic(HazelcastInstance hazelcast)
    {
        super(hazelcast, HZNames.getSchedulingTopicName());
    }
}
