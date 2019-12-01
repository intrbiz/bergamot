package com.intrbiz.bergamot.cluster.coordinator;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryMergedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.model.WorkerRegistration;
import com.intrbiz.bergamot.cluster.queue.WorkerConsumer;
import com.intrbiz.bergamot.cluster.queue.WorkerProducer;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

/**
 * Co-ordinate workers available to schedulers.
 */
public class WorkerCoordinator
{
    private static final Logger logger = Logger.getLogger(WorkerCoordinator.class);

    private static final UUID ANY_SITE = new UUID(0L, 0L);
    
    protected final SecureRandom random = new SecureRandom();
    
    protected final HazelcastInstance hazelcast;
    
    protected final Cluster cluster;
    
    protected final IMap<UUID, WorkerRegistration> workers;
    
    private volatile ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> routingTable;
    
    private final Object routeTableWriteLock = new Object();
    
    public WorkerCoordinator(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.cluster = this.hazelcast.getCluster();
        // Get our state maps
        this.workers = this.hazelcast.getMap(ObjectNames.buildWorkerRegistrationsMapName());
        // local routing table
        this.routingTable = buildRouteTable(this.workers);
        // listeners
        this.workers.addEntryListener(new WorkerListener(), true);
    }

    public Collection<WorkerRegistration> getWorkers()
    {
        return this.workers.values();
    }
    
    public WorkerRegistration getWorker(UUID workerId)
    {
        return this.workers.get(workerId);
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
                engineTable.merge(ANY_SITE, new UUID[] { worker.getId() }, WorkerCoordinator::mergeRoutes);
            }
            else
            {
                for (UUID siteId : worker.getRestrictedSiteIds())
                {
                    engineTable.merge(siteId, new UUID[] { worker.getId() }, WorkerCoordinator::mergeRoutes);    
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
        // TODO
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
    
    protected UUID route(UUID siteId, String workerPool, String engine)
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

    /**
     * Create a producer to send checks to a worker
     * 
     * @return a {@code WorkerProducer}
     */
    public WorkerProducer createCheckProducer()
    {
        return new WorkerProducer(this.hazelcast, this);
    }
    
    /**
     * Listen to changes in the workers map
     */
    private class WorkerListener implements EntryAddedListener<UUID, WorkerRegistration>, EntryEvictedListener<UUID, WorkerRegistration>, EntryRemovedListener<UUID, WorkerRegistration>, EntryMergedListener<UUID, WorkerRegistration>, EntryUpdatedListener<UUID, WorkerRegistration>
    {
        @Override
        public void entryUpdated(EntryEvent<UUID, WorkerRegistration> event)
        {
            onWorkerRegistered(event.getValue());
        }

        @Override
        public void entryMerged(EntryEvent<UUID, WorkerRegistration> event)
        {
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
