package com.intrbiz.bergamot.cluster.lookup.hz;

import com.hazelcast.core.HazelcastInstance;

public class HZProxyKeyClientLookup extends HZProxyKeyLookup
{
    public HZProxyKeyClientLookup(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }
}
