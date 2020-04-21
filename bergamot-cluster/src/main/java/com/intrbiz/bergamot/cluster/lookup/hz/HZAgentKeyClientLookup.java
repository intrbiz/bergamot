package com.intrbiz.bergamot.cluster.lookup.hz;

import com.hazelcast.core.HazelcastInstance;

public class HZAgentKeyClientLookup extends HZAgentKeyLookup
{
    public HZAgentKeyClientLookup(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }
}
