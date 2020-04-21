package com.intrbiz.bergamot.cluster.lookup;

import com.hazelcast.core.HazelcastInstance;

public class ProxyKeyClientLookup extends ProxyKeyLookup
{
    public ProxyKeyClientLookup(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }
}
