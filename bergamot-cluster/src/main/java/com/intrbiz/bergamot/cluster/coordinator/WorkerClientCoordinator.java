package com.intrbiz.bergamot.cluster.coordinator;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.model.WorkerRegistration;
import com.intrbiz.bergamot.cluster.queue.WorkerConsumer;

/**
 * Co-ordinate workers available to schedulers.
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
    public WorkerConsumer registerWorker(final UUID workerId, boolean proxy, String application, String info, String hostName, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines)
    {
        // We don't really want to allow multiple workers with the same id to register
        // TODO: we should probably just do the equivalent of ConcurrentMap.compute()
        if (this.workers.containsKey(workerId))
            throw new RuntimeException("Worker already registered, wait for registration to timeout or use a new id");
        // Register the worker
        logger.info("Registering worker " + workerId);
        final WorkerRegistration workerRegistration = new WorkerRegistration(workerId, proxy, application, info, hostName, restrictedSiteIds, workerPool, availableEngines);
        this.workers.put(workerId, workerRegistration);
        // Build the consumer for this registered worker
        return new WorkerConsumer(this.hazelcast.getQueue(ObjectNames.buildWorkerQueueName(workerId))) {
            @Override
            protected void updateWatchDog()
            {
                // Fetch our registration and register if needed
                WorkerRegistration currentRegistration = WorkerClientCoordinator.this.workers.get(workerId);
                if (currentRegistration == null)
                {
                    logger.info("Reregistering worker " + workerId);
                    WorkerClientCoordinator.this.workers.put(workerId, workerRegistration);
                }
            }

            @Override
            protected void unregister()
            {
                WorkerClientCoordinator.this.workers.remove(workerId);
            }
        };
    }
    
    public void registerAgent(UUID workerId, UUID agentId)
    {
        this.agents.put(agentId, workerId);
    }
    
    public void unregisterAgent(UUID workerId, UUID agentId)
    {
        this.agents.remove(agentId, workerId);
    }
}
