package com.intrbiz.bergamot.cluster.coordinator;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.coordinator.model.WorkerRegistration;
import com.intrbiz.bergamot.cluster.queue.WorkerConsumer;

/**
 * The client side worker coordinator
 */
public class WorkerClientCoordinator extends WorkerCoordinator
{
    private static final Logger logger = Logger.getLogger(WorkerClientCoordinator.class);
    
    public WorkerClientCoordinator(HazelcastInstance hazelcast)
    {
        super(hazelcast);
    }

    /**
     * Register a worker with the scheduling cluster so that is can receive checks to execute
     * @param workerId the random worker UUID
     * @param restrictedSiteIds an optional set of sites that this worker can process checks for
     * @param workerPool an optional worker pool that this worker has been placed into
     * @param availableEngines the check engines this worker has
     * @return the {@code WorkerConsumer} associated to this worker
     */
    public WorkerConsumer registerWorker(UUID workerId, boolean proxy, String application, String info, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines)
    {
        // We don't really want to allow multiple workers with the same id to register
        // TODO: we should probably just do the equivalent of ConcurrentMap.compute()
        if (this.workers.containsKey(workerId))
            throw new RuntimeException("Worker already registered, wait for registration to timeout or use a new id");
        // Register the worker
        logger.info("Registering worker " + workerId);
        WorkerRegistration reg = new WorkerRegistration(workerId, proxy, application, info, restrictedSiteIds, workerPool, availableEngines);
        this.workers.put(workerId, reg);
        // Build the consumer for this registered worker
        return new WorkerConsumer(this.hazelcast, workerId, this);
    }
    
    /**
     * Unregister a worker
     */
    public void unregisterWorker(UUID workerId)
    {
        // Remove the worker from the registration map
        this.workers.remove(workerId);
    }
}
