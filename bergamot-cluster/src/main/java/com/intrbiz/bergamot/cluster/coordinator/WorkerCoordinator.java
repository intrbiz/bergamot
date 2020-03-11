package com.intrbiz.bergamot.cluster.coordinator;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.model.WorkerRegistration;

/**
 * Co-ordinate workers available to schedulers.
 */
public abstract class WorkerCoordinator
{   
    protected final SecureRandom random = new SecureRandom();
    
    protected final HazelcastInstance hazelcast;
    
    protected final IMap<UUID, WorkerRegistration> workers;
    
    protected final IMap<UUID, UUID> agents;
    
    public WorkerCoordinator(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.configureHazelcast(this.hazelcast.getConfig());
        // Get our state maps
        this.workers = this.hazelcast.getMap(ObjectNames.buildWorkerRegistrationsMapName());
        this.agents = this.hazelcast.getMap(ObjectNames.buildAgentsMapName());
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
    }

    public Collection<WorkerRegistration> getWorkers()
    {
        return this.workers.values();
    }
    
    public WorkerRegistration getWorker(UUID workerId)
    {
        return this.workers.get(workerId);
    }
    
    public UUID getWorkerForAgent(UUID agentId)
    {
        return this.agents.get(agentId);
    }
}
