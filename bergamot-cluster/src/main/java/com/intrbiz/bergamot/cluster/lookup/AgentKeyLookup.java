package com.intrbiz.bergamot.cluster.lookup;

import java.util.Objects;
import java.util.UUID;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.model.AgentKey;

public abstract class AgentKeyLookup
{
    protected final HazelcastInstance hazelcast;
    
    protected final IMap<UUID, AgentKey> agentKeys;
    
    public AgentKeyLookup(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.configureHazelcast(this.hazelcast.getConfig());
        this.agentKeys = this.hazelcast.getMap(ObjectNames.buildAgentKeyLookupMapName());
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
    }
    
    public AgentKey lookupAgentKey(UUID keyId)
    {
        return this.agentKeys.get(keyId);
    }
}
