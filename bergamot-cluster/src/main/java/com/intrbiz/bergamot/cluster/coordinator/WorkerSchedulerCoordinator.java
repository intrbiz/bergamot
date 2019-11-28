package com.intrbiz.bergamot.cluster.coordinator;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryMergedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.intrbiz.bergamot.cluster.coordinator.model.WorkerRegistration;
import com.intrbiz.bergamot.cluster.listener.ProcessingPoolListener;
import com.intrbiz.bergamot.cluster.queue.ResultConsumer;
import com.intrbiz.bergamot.cluster.queue.WorkerProducer;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

/**
 * Scheduler side worker coordinator
 */
public class WorkerSchedulerCoordinator extends WorkerCoordinator
{
    private static final Logger logger = Logger.getLogger(WorkerSchedulerCoordinator.class);
    
    private static final UUID ANY_SITE = new UUID(0L, 0L);
    
    private volatile ConcurrentMap<String, ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>>> routingTable;
    
    private final Object routeTableWriteLock = new Object();

    public WorkerSchedulerCoordinator(HazelcastInstance hazelcast)
    {
        super(hazelcast);
        // local routing table
        this.routingTable = buildRouteTable(this.workers);
        // listeners
        this.workers.addEntryListener(new WorkerListener(), true);
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
                engineTable.merge(ANY_SITE, new UUID[] { worker.getId() }, WorkerSchedulerCoordinator::mergeRoutes);
            }
            else
            {
                for (UUID siteId : worker.getRestrictedSiteIds())
                {
                    engineTable.merge(siteId, new UUID[] { worker.getId() }, WorkerSchedulerCoordinator::mergeRoutes);    
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
     * Register a processing pool
     * @param id the processing pool id
     */
    public void registerProcessingPool(UUID poolId, String memberName)
    {
        logger.info("Registering processing pool " + poolId);
    }
    
    public void registerProcessPoolListener(UUID processingPooldId, ProcessingPoolListener listener)
    {
        this.processingPoolSites.addEntryListener(new ProcessingPoolListenerAdapter(listener, processingPooldId), true);
    }
    
    public void registerProcessPoolListener(ProcessingPoolListener listener)
    {
        this.processingPoolSites.addEntryListener(new ProcessingPoolListenerAdapter(listener, null), true);
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
     * @param poolId the processing pool id
     * @return a {@code WorkerProducer}
     */
    public WorkerProducer createCheckProducer(UUID poolId)
    {
        return new WorkerProducer(this.hazelcast, poolId, this);
    }
    
    /**
     * Create a consumer for check results
     * 
     * @param poolId the processing pool id
     * @return a {@code ResultConsumer}
     */
    public ResultConsumer createResultConsumer(UUID poolId)
    {
        return new ResultConsumer(this.hazelcast, poolId, this);
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
    
    private class ProcessingPoolListenerAdapter implements EntryAddedListener<UUID, UUID>, EntryRemovedListener<UUID, UUID>, EntryUpdatedListener<UUID, UUID>
    {
        private final ProcessingPoolListener listener;
        
        private final UUID filter;
        
        public ProcessingPoolListenerAdapter(ProcessingPoolListener lister, UUID filter)
        {
            super();
            this.listener = lister;
            this.filter = filter;
        }

        @Override
        public void entryUpdated(EntryEvent<UUID, UUID> event)
        {
            logger.info("Processing Pool Assignment updated " + event.getKey() + " " + event.getValue() + " (" + event.getOldValue() + ")");
            if (! event.getValue().equals(event.getOldValue()))
            {
                if (event.getOldValue() != null) 
                {
                    this.entryRemoved(event);
                }
                this.entryAdded(event);
            }
        }

        @Override
        public void entryRemoved(EntryEvent<UUID, UUID> event)
        {
            logger.info("Processing Pool Assignment removed " + event.getKey() + " " + event.getValue() + " (" + event.getOldValue() + ")");
            if (filter == null || filter.equals(event.getOldValue()))
            {
                int processingPool = Site.extractSiteProcessingPool(event.getKey());
                UUID siteId = Site.getSiteId(event.getKey());
                this.listener.sitePoolAssigned(siteId, processingPool, event.getOldValue());
            }
        }

        @Override
        public void entryAdded(EntryEvent<UUID, UUID> event)
        {
            logger.info("Processing Pool Assignment added " + event.getKey() + " " + event.getValue() + " (" + event.getOldValue() + ")");
            if (filter == null || filter.equals(event.getValue()))
            {
                int processingPool = Site.extractSiteProcessingPool(event.getKey());
                UUID siteId = Site.getSiteId(event.getKey());
                this.listener.sitePoolAssigned(siteId, processingPool, event.getValue());
            }
        }
    }
}
