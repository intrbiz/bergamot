package com.intrbiz.bergamot.cluster.coordinator;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.queue.ProcessingPoolProducer;

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
public class ProcessingPoolClientCoordinator extends ProcessingPoolCoordinator
{
    public ProcessingPoolClientCoordinator(HazelcastInstance hazelcastInstance)
    {
        super(hazelcastInstance);
    }
}
