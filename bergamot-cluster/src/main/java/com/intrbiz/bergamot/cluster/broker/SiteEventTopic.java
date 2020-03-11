package com.intrbiz.bergamot.cluster.broker;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.message.event.site.SiteEvent;

public class SiteEventTopic extends GenericGlobalTopic<SiteEvent>
{
    public SiteEventTopic(HazelcastInstance hazelcast)
    {
        super(hazelcast, ObjectNames.getSiteEventTopicName());
    }
}
