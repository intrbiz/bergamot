package com.intrbiz.bergamot.cluster.lookup.hz;

import java.util.Objects;
import java.util.UUID;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.AgentKey;

public abstract class HZAgentKeyLookup implements AgentKeyLookup
{
    protected final HazelcastInstance hazelcast;
    
    protected final IMap<UUID, AgentKey> agentKeys;
    
    public HZAgentKeyLookup(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.configureHazelcast(this.hazelcast.getConfig());
        this.agentKeys = this.hazelcast.getMap(HZNames.buildAgentKeyLookupMapName());
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
    }
    
    public AgentKey lookupAgentKey(UUID keyId)
    {
        return this.agentKeys.get(keyId);
    }
}
