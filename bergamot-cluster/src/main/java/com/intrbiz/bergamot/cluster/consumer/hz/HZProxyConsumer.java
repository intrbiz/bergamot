package com.intrbiz.bergamot.cluster.consumer.hz;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.consumer.ProxyConsumer;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.proxy.ProxyMessage;

public class HZProxyConsumer extends HZBaseConsumer<ProxyMessage> implements ProxyConsumer
{
    public HZProxyConsumer(HazelcastInstance hazelcast, UUID id)
    {
        super(hazelcast, id, HZNames::buildProxyRingbufferName, HZNames::buildProxiesSequenceMapName);
    }
}
