package com.intrbiz.bergamot.cluster.lookup;

import com.hazelcast.core.HazelcastInstance;

public class AgentKeyClientLookup extends AgentKeyLookup
{
    public AgentKeyClientLookup(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }
}
