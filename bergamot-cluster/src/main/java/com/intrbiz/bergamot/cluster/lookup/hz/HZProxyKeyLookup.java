package com.intrbiz.bergamot.cluster.lookup.hz;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.intrbiz.bergamot.cluster.lookup.ProxyKeyLookup;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.ProxyKey;

public abstract class HZProxyKeyLookup implements ProxyKeyLookup
{
    protected final HazelcastInstance hazelcast;
    
    protected final IMap<UUID, ProxyKey> proxyKeys;
    
    public HZProxyKeyLookup(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.configureHazelcast(this.hazelcast.getConfig());
        this.proxyKeys = this.hazelcast.getMap(HZNames.buildProxyKeyLookupMapName());
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
    }
    
    public CompletionStage<ProxyKey> lookupProxyKey(UUID keyId)
    {
        return this.proxyKeys.getAsync(keyId);
    }
}
