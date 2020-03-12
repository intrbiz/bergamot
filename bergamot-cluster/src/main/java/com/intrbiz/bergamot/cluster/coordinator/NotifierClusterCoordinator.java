package com.intrbiz.bergamot.cluster.coordinator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.collection.IQueue;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import com.hazelcast.spi.exception.TargetDisconnectedException;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.model.NotifierRegistration;
import com.intrbiz.bergamot.cluster.queue.NotificationProducer;
import com.intrbiz.bergamot.model.message.notification.Notification;

/**
 * Co-ordinate notifiers
 */
public class NotifierClusterCoordinator extends NotifierCoordinator implements NotificationProducer
{
    private static final Logger logger = Logger.getLogger(NotifierClusterCoordinator.class);
    
    private static final int NOTIFIER_MAX_IDLE_SECONDS = 30;
    
    protected static final UUID ANY_SITE = new UUID(0,0);
    
    protected final Cluster cluster;
    
    private final ConcurrentMap<UUID, IQueue<Notification>> notifierQueueCache = new ConcurrentHashMap<>();
    
    /**
     * Notification routing table:
     * engine -> site -> workers
     */
    private volatile ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> routingTable;
    
    private final Object routeTableWriteLock = new Object();
    
    private final IQueue<UUID> cleanupQueue;
    
    private final IQueue<Notification> deadQueue;
    
    private Thread cleanupThread;
    
    private volatile boolean run;
    
    public NotifierClusterCoordinator(HazelcastInstance hazelcast)
    {
        super(hazelcast);
        this.cluster = this.hazelcast.getCluster();
        // clean up queue
        this.cleanupQueue = this.hazelcast.getQueue(ObjectNames.buildNotifierCleanupQueueName());
        this.deadQueue = this.hazelcast.getQueue(ObjectNames.buildNotifierDeadQueueName());
        // local routing table
        this.routingTable = buildRouteTable(this.notifiers);
        // listeners
        this.notifiers.addEntryListener(new NotifierListener(), true);
    }
    
    protected void configureHazelcast(Config hazelcastConfig)
    {
        // Configure TTLs for the worker map
        MapConfig workerMap = hazelcastConfig.getMapConfig(ObjectNames.buildNotifierRegistrationsMapName());
        workerMap.setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU));
        workerMap.setMaxIdleSeconds(NOTIFIER_MAX_IDLE_SECONDS);
    }
    
    public ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> getRoutingTable()
    {
        return this.routingTable;
    }
    
    // 
    
    public void start()
    {
        this.run = true;
        this.cleanupThread = new Thread(this::cleanupRunLoop, "bergamot-notifier-cleanup");
        this.cleanupThread.setDaemon(false);
        this.cleanupThread.start();
    }
    
    protected void cleanupRunLoop()
    {
        while (this.run)
        {
            try
            {
                UUID notifierId = this.cleanupQueue.poll(2, TimeUnit.SECONDS);
                if (notifierId != null)
                {
                    this.cleanupNotifier(notifierId);
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
    
    protected void cleanupNotifier(UUID notifierId)
    {
        logger.info("Cleaning up after notifier " + notifierId + " left us.");
        IQueue<Notification> notifierQueue = this.hazelcast.getQueue(ObjectNames.buildNotifierQueueName(notifierId));
        Notification notification;
        while ((notification = notifierQueue.poll()) != null)
        {
            // Reroute the notification
            UUID target = this.routeNotification(notification);
            if (target != null)
            {
                this.hazelcast.getQueue(ObjectNames.buildNotifierQueueName(target))
                    .offer(notification);
            }
            else
            {
                // Place into the dead queue
                this.deadQueue.offer(notification);
            }
        }
        notifierQueue.destroy();
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
    
    protected IQueue<Notification> getNotifierQueue(UUID notifierId)
    {
        return this.notifierQueueCache.computeIfAbsent(notifierId, 
                (id) -> this.hazelcast.getQueue(ObjectNames.buildNotifierQueueName(id)));
    }
    
    // Event handlers
    
    protected void onNotifierRegistered(NotifierRegistration notifier)
    {
        logger.info("Received notifier registration: " + notifier.getId());
        // Add routes
        this.addRoutes(notifier);
    }
    
    protected void onNotifierDeregistered(UUID notifierId)
    {
        logger.info("Received notifier deregistration: " + notifierId);
        // Remove routes
        this.removeRoutes(notifierId);
        // Add the notifier to the clean up queue
        this.cleanupQueue.offer(notifierId);
        // Remove the cached notifier queues
        this.notifierQueueCache.remove(notifierId);
    }
    
    // routing
    
    protected static final ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> buildRouteTable(IMap<UUID, NotifierRegistration> workers)
    {
        ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> table = new ConcurrentHashMap<>();
        for (NotifierRegistration worker : workers.values())
        {
            addRoutesToTable(table, worker);
        }
        logger.info("Build notifier routing table: " + table);
        return table;
    }
    
    protected static final void addRoutesToTable(ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> table, NotifierRegistration worker)
    {
        // Register the route for each engine
        for (String engine : worker.getAvailableEngines())
        {
            ConcurrentMap<UUID, UUID[]> engineTable =  table.computeIfAbsent(engine, (key) -> new ConcurrentHashMap<>());
            // Merge in the routes
            if (worker.getRestrictedSiteIds() == null || worker.getRestrictedSiteIds().length == 0)
            {
                engineTable.merge(ANY_SITE, new UUID[] { worker.getId() }, NotifierClusterCoordinator::mergeRoutes);
            }
            else
            {
                for (UUID siteId : worker.getRestrictedSiteIds())
                {
                    engineTable.merge(siteId, new UUID[] { worker.getId() }, NotifierClusterCoordinator::mergeRoutes);    
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
    
    protected static final void removeRoutesFromTable(ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> table, UUID notifierId)
    {
        for (String engine : table.keySet())
        {
            table.compute(engine, (key, value) -> removeRoutesFromSiteTable(value, notifierId));
        }
    }
    
    protected static final ConcurrentMap<UUID, UUID[]> removeRoutesFromSiteTable(ConcurrentMap<UUID, UUID[]> table, UUID notifierId)
    {
        for (UUID site : table.keySet())
        {
            table.compute(site, (key, value) -> removeRoutes(value, notifierId));
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
    
    protected void addRoutes(NotifierRegistration worker)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.info("Adding notifier " + worker + " to routing table");
            addRoutesToTable(this.routingTable, worker);
        }
    }
    
    protected void removeRoutes(UUID workerId)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.info("Removing notifier " + workerId + " to routing table");
            removeRoutesFromTable(this.routingTable, workerId);
        }
    }
    
    protected UUID routeNotification(UUID siteId, String engine)
    {
        ConcurrentMap<UUID, UUID[]> engineTable =  this.routingTable.get(engine);
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
        return null;
    }
    
    protected UUID routeNotification(Notification notification)
    {
        return this.routeNotification(notification.getSite().getId(), notification.getEngine());
    }
    
    protected UUID routeNotification(Notification notification, String engine)
    {
        return this.routeNotification(notification.getSite().getId(), engine);
    }
    
    public Set<String> getAvailableEngines()
    {
        return new HashSet<String>(this.routingTable.keySet());
    }
    
    /**
     * Send a notification
     * @param notification the notification to send
     */
    @Override
    public void sendNotification(Notification notification)
    {
        for (String engine : this.getAvailableEngines())
        {
            UUID target = this.routeNotification(notification, engine);
            if (target != null)
            {
                this.getNotifierQueue(target)
                    .offer(notification.cloneNotification(engine));
            }
        }
    }
    
    /**
     * Listen to changes in the notifier map
     */
    private class NotifierListener implements EntryAddedListener<UUID, NotifierRegistration>, EntryEvictedListener<UUID, NotifierRegistration>, EntryRemovedListener<UUID, NotifierRegistration>, EntryUpdatedListener<UUID, NotifierRegistration>
    {
        @Override
        public void entryUpdated(EntryEvent<UUID, NotifierRegistration> event)
        {
            // TODO: Look at the differences between the registration
            onNotifierRegistered(event.getValue());
        }

        @Override
        public void entryRemoved(EntryEvent<UUID, NotifierRegistration> event)
        {
            onNotifierDeregistered(event.getKey());
        }

        @Override
        public void entryEvicted(EntryEvent<UUID, NotifierRegistration> event)
        {
            onNotifierDeregistered(event.getKey());
        }

        @Override
        public void entryAdded(EntryEvent<UUID, NotifierRegistration> event)
        {
            onNotifierRegistered(event.getValue());
        }
    }
}
