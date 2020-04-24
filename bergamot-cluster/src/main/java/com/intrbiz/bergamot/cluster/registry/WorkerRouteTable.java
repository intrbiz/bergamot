package com.intrbiz.bergamot.cluster.registry;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * A routing table of Bergamot Workers.
 */
public class WorkerRouteTable
{
    public static final UUID ANY_SITE = new UUID(0,0);
    
    private static final Logger logger = Logger.getLogger(WorkerRouteTable.class);
    
    /**
     * Worker routing table:
     *     worker_pool -> engine -> site -> workers
     */
    private final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> routingTable = new ConcurrentHashMap<>();
    
    private final SecureRandom random = new SecureRandom();
    
    private final Object routeTableWriteLock = new Object();
    
    public WorkerRouteTable()
    {
        super();
    }
    
    private static final String coalesceWorkerPool(String workerPool)
    {
        return workerPool == null || workerPool.length() == 0 ? "any" : workerPool;
    }
    
    private void addRoutesToTable(WorkerRegistration worker)
    {
        // Get the table for the given worker pool
        ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> workerPoolTable = this.routingTable.computeIfAbsent(coalesceWorkerPool(worker.getWorkerPool()), (key) -> new ConcurrentHashMap<>());
        // Register the route for each engine
        for (String engine : worker.getAvailableEngines())
        {
            ConcurrentMap<UUID, UUID[]> engineTable =  workerPoolTable.computeIfAbsent(engine, (key) -> new ConcurrentHashMap<>());
            // Merge in the routes
            if (worker.getRestrictedSiteIds() == null || worker.getRestrictedSiteIds().isEmpty())
            {
                engineTable.merge(ANY_SITE, new UUID[] { worker.getId() }, WorkerRouteTable::mergeRoutes);
            }
            else
            {
                for (UUID siteId : worker.getRestrictedSiteIds())
                {
                    engineTable.merge(siteId, new UUID[] { worker.getId() }, WorkerRouteTable::mergeRoutes);    
                }
            }
        }
    }
    
    private static final UUID[] mergeRoutes(UUID[] oldValue, UUID[] value)
    {
        HashSet<UUID> merged = new HashSet<UUID>();
        if (oldValue != null) Collections.addAll(merged, oldValue);
        if (value != null) Collections.addAll(merged, value);
        return merged.toArray(new UUID[merged.size()]);
    }
    
    public void removeRoutesFromTable(UUID workerId)
    {
        for (String workerPool : this.routingTable.keySet())
        {
            this.routingTable.compute(workerPool, (key, value) -> removeRoutesFromEngineTable(value, workerId));
        }
    }
    
    private static final ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> removeRoutesFromEngineTable(ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> table, UUID workerId)
    {
        for (String engine : table.keySet())
        {
            table.compute(engine, (key, value) -> removeRoutesFromSiteTable(value, workerId));
        }
        return table.isEmpty() ? null : table;
    }
    
    private static final ConcurrentMap<UUID, UUID[]> removeRoutesFromSiteTable(ConcurrentMap<UUID, UUID[]> table, UUID workerId)
    {
        for (UUID site : table.keySet())
        {
            table.compute(site, (key, value) -> removeRoutes(value, workerId));
        }
        return table.isEmpty() ? null : table;
    }
    
    private static final UUID[] removeRoutes(UUID[] value, UUID toRemove)
    {
        HashSet<UUID> removed = new HashSet<UUID>();
        if (value != null) Collections.addAll(removed, value);
        if (toRemove != null) removed.remove(toRemove);
        return removed.isEmpty() ? null : removed.toArray(new UUID[removed.size()]);
    }
    
    /**
     * Register a single worker into the route table
     * @param worker the worker to add
     */
    void registerWorker(WorkerRegistration worker)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.debug("Adding worker " + worker + " to routing table");
            this.addRoutesToTable(worker);
        }
    }
    
    /**
     * Register multiple workers into the route table
     * @param workers the workers to add
     */
    void registerWorkers(Set<WorkerRegistration> workers)
    {
        synchronized (this.routeTableWriteLock)
        {
            this.routingTable.clear();
            for (WorkerRegistration worker : workers)
            {
                logger.debug("Adding worker " + worker + " to routing table");
                this.addRoutesToTable(worker);
            }
        }
    }
    
    /**
     * Remove a single worker from the route table
     * @param workerId the id of the worker to remove
     */
    void unregisterWorker(UUID workerId)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.debug("Removing worker " + workerId + " to routing table");
            this.removeRoutesFromTable(workerId);
        }
    }
    
    /**
     * Reregister a single worker into the route table
     * @param worker the worker to add
     */
    void reregisterWorker(WorkerRegistration worker)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.debug("Updating worker " + worker + " to routing table");
            this.unregisterWorker(worker.getId());
            this.registerWorker(worker);
        }
    }
    
    /**
     * Route a check to a worker
     * @param siteId the site id of the check
     * @param workerPool the worker pool of the check
     * @param engine the engine of the check
     * @return the worker id
     */
    public UUID route(UUID siteId, String workerPool, String engine)
    {
        // worker_pool -> engine -> site -> workers
        ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> workerPoolTable = this.routingTable.get(coalesceWorkerPool(workerPool));
        if (workerPoolTable != null)
        {
            ConcurrentMap<UUID, UUID[]> engineTable =  workerPoolTable.get(engine);
            if (engineTable != null)
            {
                // get the routes for the given site, fall back to any site
                UUID[] workers = engineTable.get(siteId);
                if (workers == null)
                    workers = engineTable.get(ANY_SITE);
                if (workers != null)
                {
                    // Choose a random route
                    return workers[Math.abs(this.random.nextInt() % workers.length)];
                }
            }
        }
        return null;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        // worker_pool -> engine -> site -> workers
        for (String workerPool: this.routingTable.keySet())
        {
            ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> engineTable = this.routingTable.get(workerPool);
            if (engineTable != null)
            {
                for (String engine : engineTable.keySet())
                {
                    ConcurrentMap<UUID, UUID[]> siteTable = engineTable.get(engine);
                    for (UUID site : siteTable.keySet())
                    {
                        UUID[] workers = siteTable.get(site);
                        if (workers != null)
                        {
                            sb.append(workerPool).append("::").append(engine).append("::").append(site).append(" => ").append(Arrays.asList(workers)).append("\n");
                        }
                    }
                }
            }
        }
        return sb.toString();
    }
}
