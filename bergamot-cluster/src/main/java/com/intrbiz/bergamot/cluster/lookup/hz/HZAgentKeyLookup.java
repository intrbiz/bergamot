package com.intrbiz.bergamot.cluster.lookup.hz;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.agent.AgentAuthenticationKey;

public abstract class HZAgentKeyLookup implements AgentKeyLookup
{
    protected final HazelcastInstance hazelcast;
    
    protected final IMap<UUID, AgentAuthenticationKey> agentKeys;
    
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
    
    public void lookupAgentKey(UUID keyId, Consumer<AgentAuthenticationKey> callback)
    {
        this.agentKeys.getAsync(keyId).whenComplete((key, error) -> callback.accept(key));
    }
}
