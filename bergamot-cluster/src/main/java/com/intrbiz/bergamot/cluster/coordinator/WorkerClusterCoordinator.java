package com.intrbiz.bergamot.cluster.coordinator;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.spi.exception.TargetDisconnectedException;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.model.WorkerRegistration;
import com.intrbiz.bergamot.cluster.queue.WorkerProducer;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

/**
 * Co-ordinate workers available to schedulers.
 */
public class WorkerClusterCoordinator extends WorkerCoordinator implements WorkerProducer
{
    private static final Logger logger = Logger.getLogger(WorkerClusterCoordinator.class);
    
    private static final int WORKER_MAX_IDLE_SECONDS = 30;
    
    protected static final UUID ANY_SITE = new UUID(0,0);
    
    protected final Cluster cluster;
    
    private final ConcurrentMap<UUID, IQueue<ExecuteCheck>> workerQueueCache = new ConcurrentHashMap<>();
    
    /**
     * Worker routing table:
     * worker_pool -> engine -> site -> workers
     */
    private volatile ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> routingTable;
    
    private final Object routeTableWriteLock = new Object();
    
    private final IQueue<UUID> cleanupQueue;
    
    private final IQueue<ExecuteCheck> deadQueue;
    
    private Thread cleanupThread;
    
    private volatile boolean run;
    
    public WorkerClusterCoordinator(HazelcastInstance hazelcast)
    {
        super(hazelcast);
        this.cluster = this.hazelcast.getCluster();
        // clean up queue
        this.cleanupQueue = this.hazelcast.getQueue(ObjectNames.buildWorkerCleanupQueueName());
        this.deadQueue = this.hazelcast.getQueue(ObjectNames.buildWorkerDeadQueueName());
        // local routing table
        this.routingTable = buildRouteTable(this.workers);
        // listeners
        this.workers.addEntryListener(new WorkerListener(), true);
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
        // Configure TTLs for the worker map
        MapConfig workerMap = hazelcastConfig.getMapConfig(ObjectNames.buildWorkerRegistrationsMapName());
        workerMap.setEvictionPolicy(EvictionPolicy.LRU);
        workerMap.setMaxIdleSeconds(WORKER_MAX_IDLE_SECONDS);
    }
    
    public ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> getRoutingTable()
    {
        return this.routingTable;
    }
    
    protected void cleanupRunLoop()
    {
        while (this.run)
        {
            try
            {
                UUID workerId = this.cleanupQueue.poll(2, TimeUnit.SECONDS);
                if (workerId != null)
                {
                    this.cleanupWorker(workerId);
                }
            }
            catch (TargetDisconnectedException | InterruptedException e)
            {
                // IGNORE
            }
            catch (Exception e)
            {
                logger.error("Error processing clean up queue", e);
            }
        }
    }
    
    protected void cleanupWorker(UUID workerId)
    {
        logger.info("Cleaning up after worker " + workerId + " left us.");
        IQueue<ExecuteCheck> workerQueue = this.hazelcast.getQueue(ObjectNames.buildWorkerQueueName(workerId));
        ExecuteCheck check;
        while ((check = workerQueue.poll()) != null)
        {
            // Reroute the check if possible
            UUID target = this.routeCheck(check);
            if (target != null)
            {
                this.hazelcast.getQueue(ObjectNames.buildWorkerQueueName(target))
                    .offer(check);
            }
            else
            {
                // Place into the dead queue
                this.deadQueue.offer(check);
            }
        }
        workerQueue.destroy();
    }
    
    public void stop()
    {
        this.run = false;
        if (this.cleanupThread != null)
        {
            try
            {
                this.cleanupThread.join();
            }
            catch (InterruptedException e)
            {
            }
            this.cleanupThread = null;
        }
    }
    
    // Event handlers
    
    protected void onWorkerRegistered(WorkerRegistration worker)
    {
        logger.info("Received worker registration: " + worker.getId());
        // Add routes
        this.addRoutes(worker);
    }
    
    protected void onWorkerDeregistered(UUID workerId)
    {
        logger.info("Received worker deregistration: " + workerId);
        // Remove routes
        this.removeRoutes(workerId);
        // Add the worker to the clean up queue
        this.cleanupQueue.offer(workerId);
    }
    
    // routing
    
    protected static final ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> buildRouteTable(IMap<UUID, WorkerRegistration> workers)
    {
        ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> table = new ConcurrentHashMap<>();
        for (WorkerRegistration worker : workers.values())
        {
            addRoutesToTable(table, worker);
        }
        logger.info("Build worker routing table: " + table);
        return table;
    }
    
    protected static final String coalesceWorkerPool(String workerPool)
    {
        return workerPool == null || workerPool.length() == 0 ? "any" : workerPool;
    }
    
    protected static final void addRoutesToTable(ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> table, WorkerRegistration worker)
    {
        // The route we are adding
        // Get the table for the given worker pool
        ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> workerPoolTable = table.computeIfAbsent(coalesceWorkerPool(worker.getWorkerPool()), (key) -> new ConcurrentHashMap<>());
        // Register the route for each engine
        for (String engine : worker.getAvailableEngines())
        {
            ConcurrentMap<UUID, UUID[]> engineTable =  workerPoolTable.computeIfAbsent(engine, (key) -> new ConcurrentHashMap<>());
            // Merge in the routes
            if (worker.getRestrictedSiteIds() == null || worker.getRestrictedSiteIds().length == 0)
            {
                engineTable.merge(ANY_SITE, new UUID[] { worker.getId() }, WorkerClusterCoordinator::mergeRoutes);
            }
            else
            {
                for (UUID siteId : worker.getRestrictedSiteIds())
                {
                    engineTable.merge(siteId, new UUID[] { worker.getId() }, WorkerClusterCoordinator::mergeRoutes);    
                }
            }
        }
    }
    
    protected static final UUID[] mergeRoutes(UUID[] oldValue, UUID[] value)
    {
        HashSet<UUID> merged = new HashSet<UUID>();
        if (oldValue != null) Collections.addAll(merged, oldValue);
        if (value != null) Collections.addAll(merged, value);
        return merged.toArray(new UUID[merged.size()]);
    }
    
    protected static final void removeRoutesFromTable(ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> table, UUID workerId)
    {
        for (String workerPool : table.keySet())
        {
            table.compute(workerPool, (key, value) -> removeRoutesFromEngineTable(value, workerId));
        }
    }
    
    protected static final ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> removeRoutesFromEngineTable(ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> table, UUID workerId)
    {
        for (String engine : table.keySet())
        {
            table.compute(engine, (key, value) -> removeRoutesFromSiteTable(value, workerId));
        }
        return table.isEmpty() ? null : table;
    }
    
    protected static final ConcurrentMap<UUID, UUID[]> removeRoutesFromSiteTable(ConcurrentMap<UUID, UUID[]> table, UUID workerId)
    {
        for (UUID site : table.keySet())
        {
            table.compute(site, (key, value) -> removeRoutes(value, workerId));
        }
        return table.isEmpty() ? null : table;
    }
    
    protected static final UUID[] removeRoutes(UUID[] value, UUID toRemove)
    {
        HashSet<UUID> removed = new HashSet<UUID>();
        if (value != null) Collections.addAll(removed, value);
        if (toRemove != null) removed.remove(toRemove);
        return removed.isEmpty() ? null : removed.toArray(new UUID[removed.size()]);
    }
    
    protected void addRoutes(WorkerRegistration worker)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.info("Adding worker " + worker + " to routing table");
            addRoutesToTable(this.routingTable, worker);
        }
    }
    
    protected void removeRoutes(UUID workerId)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.info("Removing worker " + workerId + " to routing table");
            removeRoutesFromTable(this.routingTable, workerId);
        }
    }
    
    public UUID route(UUID siteId, String workerPool, String engine)
    {
        ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> workerPoolTable = this.routingTable.get(coalesceWorkerPool(workerPool));
        if (workerPoolTable != null)
        {
            ConcurrentMap<UUID, UUID[]> engineTable =  workerPoolTable.get(engine);
            if (engineTable != null)
            {
                // get the routes for the given site, fall back to any site
                UUID[] routes = engineTable.get(siteId);
                if (routes == null)
                    routes = engineTable.get(ANY_SITE);
                if (routes != null)
                {
                    // Choose a random route
                    return routes[this.random.nextInt(routes.length)];
                }
            }
        }
        return null;
    }

    /**
     * Route the given check to a worker
     * 
     * @param check
     * @return
     */
    public UUID routeCheck(ExecuteCheck check)
    {
        return this.route(check.getSiteId(), check.getWorkerPool(), check.getEngine());
    }

    private IQueue<ExecuteCheck> getCheckQueue(UUID workerId)
    {
        return this.workerQueueCache.computeIfAbsent(workerId, (key) -> {
            return this.hazelcast.getQueue(ObjectNames.buildWorkerQueueName(key));
        });
    }
    
    /**
     * Execute a check by assigning it to a worker
     * @param check the check to execute
     * @return if the check was successfully given to a worker
     */
    @Override
    public PublishStatus executeCheck(ExecuteCheck check)
    {
        // Pick a worker for this check
        UUID workerId = this.routeCheck(check);
        if (workerId == null) return PublishStatus.Unroutable;
        // Get the worker queue
        IQueue<ExecuteCheck> queue = this.getCheckQueue(workerId);
        // Offer onto the worker queue
        boolean success = queue.offer(check);
        return success ? PublishStatus.Success : PublishStatus.Failed;
    }
    
    /**
     * Listen to changes in the workers map
     */
    private class WorkerListener implements EntryAddedListener<UUID, WorkerRegistration>, EntryEvictedListener<UUID, WorkerRegistration>, EntryRemovedListener<UUID, WorkerRegistration>, EntryUpdatedListener<UUID, WorkerRegistration>
    {
        @Override
        public void entryUpdated(EntryEvent<UUID, WorkerRegistration> event)
        {
            // TODO: Look at the differences between the registration
            onWorkerRegistered(event.getValue());
        }

        @Override
        public void entryRemoved(EntryEvent<UUID, WorkerRegistration> event)
        {
            onWorkerDeregistered(event.getKey());
        }

        @Override
        public void entryEvicted(EntryEvent<UUID, WorkerRegistration> event)
        {
            onWorkerDeregistered(event.getKey());
        }

        @Override
        public void entryAdded(EntryEvent<UUID, WorkerRegistration> event)
        {
            onWorkerRegistered(event.getValue());
        }
    }
}
