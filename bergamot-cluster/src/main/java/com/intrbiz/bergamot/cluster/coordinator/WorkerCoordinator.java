package com.intrbiz.bergamot.cluster.coordinator;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.intrbiz.bergamot.cluster.coordinator.model.WorkerRegistration;
import com.intrbiz.bergamot.model.Site;

/**
 * Co-ordinate workers available to schedulers.
 *
 */
public abstract class WorkerCoordinator
{

    protected final SecureRandom random = new SecureRandom();
    
    protected final HazelcastInstance hazelcast;
    
    protected final Cluster cluster;
    
    protected final IMap<UUID, WorkerRegistration> workers;
    
    /**
     * Mapping of site processing pools to processing pool
     */
    protected final IMap<UUID, UUID> processingPoolSites;
    
    public WorkerCoordinator(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.cluster = this.hazelcast.getCluster();
        // Get our state maps
        this.workers = this.hazelcast.getMap(ClusterNames.buildWorkerRegistrationsMapName());
        this.processingPoolSites = this.hazelcast.getMap(ClusterNames.buildProcessingPoolSitesMapName());
    }
    
    public List<Member> getProcessingPools()
    {
        List<Member> processingPools = new LinkedList<>();
        for (Member member : this.cluster.getMembers())
        {
            if (Boolean.TRUE.equals(member.getBooleanAttribute(ClusterNames.Attributes.MEMBER_TYPE_PROCESSOR)))
                processingPools.add(member);
        }
        return processingPools;
    }
    
    public Member getProcessingPool(UUID poolId)
    {
        String id = poolId.toString();
        for (Member member : this.cluster.getMembers())
        {
            if (id.equals(member.getUuid())
                    && Boolean.TRUE.equals(member.getBooleanAttribute(ClusterNames.Attributes.MEMBER_TYPE_PROCESSOR)))
                return member;
        }
        return null;
    }
    
    public boolean canRegisterWorkers()
    {
        return this.getProcessingPools().size() > 0;
    }

    public Collection<WorkerRegistration> getWorkers()
    {
        return this.workers.values();
    }
    
    public WorkerRegistration getWorker(UUID workerId)
    {
        return this.workers.get(workerId);
    }
    
    public UUID getProcessPoolForCheck(UUID objectId)
    {
        return this.processingPoolSites.get(Site.getSiteProcessingPool(objectId));
    }
}
