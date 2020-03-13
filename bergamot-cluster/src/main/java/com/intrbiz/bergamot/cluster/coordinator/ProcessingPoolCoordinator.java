package com.intrbiz.bergamot.cluster.coordinator;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.model.ProcessingPoolRegistration;
import com.intrbiz.bergamot.cluster.queue.ProcessingPoolProducer;
import com.intrbiz.bergamot.cluster.queue.SchedulerActionProducer;
import com.intrbiz.bergamot.model.Site;

/**
 * Manage scheduling and result processing services across the cluster
 * 
 * Every site has a (configurable) number of processing pools, checks 
 * are split across these pools. The processing pools are then split 
 * across the members within the cluster. As such work should be 
 * balanced across the scheduling and result processing resources 
 * of the cluster.
 * 
 * This cluster manager is handle migrating resources around the 
 * cluster. The general idea is that when the cluster state changes, 
 * the cluster members will race to acquire the management lock. 
 * The winner will then decide the state of the cluster and issue 
 * the required migration tasks.
 * 
 * Migrations are placed into a queue named with the UUID of the 
 * cluster member which should execute them.  A cluster member will 
 * consume and apply these migration tasks
 * 
 */
public abstract class ProcessingPoolCoordinator
{   
    protected final HazelcastInstance hazelcastInstance;

    protected final Cluster cluster;
    
    /**
     * Map of which members are processors
     */
    protected final IMap<UUID, Boolean> processors;
    
    /**
     * The distributed map of site processing pools
     */
    protected final IMap<UUID, ProcessingPoolRegistration> pools;
    
    protected final SecureRandom random = new SecureRandom();

    public ProcessingPoolCoordinator(HazelcastInstance hazelcastInstance)
    {
        super();
        this.hazelcastInstance = hazelcastInstance;
        this.configureHazelcast(this.hazelcastInstance.getConfig());
        this.cluster = this.hazelcastInstance.getCluster();
        this.processors = this.hazelcastInstance.getMap(ObjectNames.buildProcessorsMapName());
        this.pools = this.hazelcastInstance.getMap(ObjectNames.buildProcessingPoolsMapName());
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
    }
    
    public List<Member> getProcessingMembers()
    {
        return this.cluster.getMembers().stream()
                .filter(this::isProcessingPool)
                .collect(Collectors.toList());
    }
    
    public boolean isProcessingPool(Member member)
    {
        return Boolean.TRUE.equals(this.processors.get(member.getUuid()));
    }
    
    /**
     * Route a passive check to a processing pool
     * @param siteId the site the check is part of
     * @return the processing pool id
     */
    public UUID routeActiveCheck(UUID checkId)
    {
        ProcessingPoolRegistration poolReg = this.pools.get(Site.getSiteProcessingPool(checkId));
        return poolReg == null ? null : poolReg.getOwner();
    }
    
    /**
     * Route a passive check to a processing pool
     * @param siteId the site the check is part of
     * @return the processing pool id
     */
    public UUID routePassiveCheck(UUID siteId)
    {
        int pool = this.random.nextInt(Site.PROCESSING_POOL_COUNT);
        ProcessingPoolRegistration poolReg = this.pools.get(Site.getSiteProcessingPool(siteId, pool));
        if (poolReg == null)
        {
            poolReg = this.pools.get(Site.getSiteProcessingPool(siteId, 0));
        }
        return poolReg == null ? null : poolReg.getOwner();
    }
    
    public ProcessingPoolProducer createProcessingPoolProducer()
    {
        return new ProcessingPoolProducer(this.hazelcastInstance, this);
    }
    
    public SchedulerActionProducer createSchedulerActionProducer()
    {
        return new SchedulerActionProducer(this.hazelcastInstance, this);
    }
}
