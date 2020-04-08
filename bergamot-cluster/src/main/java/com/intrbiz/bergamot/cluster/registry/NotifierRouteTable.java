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

import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;

/**
 * A routing table of Bergamot Notifiers.
 */
public class NotifierRouteTable
{
    public static final UUID ANY_SITE = new UUID(0,0);
    
    private static final Logger logger = Logger.getLogger(NotifierRouteTable.class);
    
    /**
     * Notification routing table:
     * engine -> site -> notifiers
     */
    private volatile ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> routingTable = new ConcurrentHashMap<>();
    
    private final SecureRandom random = new SecureRandom();
    
    private final Object routeTableWriteLock = new Object();
    
    public NotifierRouteTable()
    {
        super();
    }
    
    protected void addRoutesToTable(NotifierRegistration worker)
    {
        // Register the route for each engine
        for (String engine : worker.getAvailableEngines())
        {
            ConcurrentMap<UUID, UUID[]> engineTable =  this.routingTable.computeIfAbsent(engine, (key) -> new ConcurrentHashMap<>());
            // Merge in the routes
            if (worker.getRestrictedSiteIds() == null || worker.getRestrictedSiteIds().isEmpty())
            {
                engineTable.merge(ANY_SITE, new UUID[] { worker.getId() }, NotifierRouteTable::mergeRoutes);
            }
            else
            {
                for (UUID siteId : worker.getRestrictedSiteIds())
                {
                    engineTable.merge(siteId, new UUID[] { worker.getId() }, NotifierRouteTable::mergeRoutes);    
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
    
    protected void removeRoutesFromTable(ConcurrentMap<String, ConcurrentMap<UUID, UUID[]>> table, UUID notifierId)
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
    
    /**
     * Register a single notifier into the route table
     * @param notifier the notifier to register
     */
    void registerNotifier(NotifierRegistration notifier)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.debug("Adding notifier " + notifier + " to routing table");
            this.addRoutesToTable(notifier);
        }
    }
    
    /**
     * Register multiple notifier into the route table
     * @param notifiers the notifiers to register
     */
    void registerNotifiers(Set<NotifierRegistration> notifiers)
    {
        synchronized (this.routeTableWriteLock)
        {
            for (NotifierRegistration notifier : notifiers)
            {
                logger.debug("Adding notifier " + notifier + " to routing table");
                this.addRoutesToTable(notifier);
            }
        }
    }
    
    /**
     * Remove the specific notifier from the route table
     * @param notifierId the notifier id to remove
     */
    void unregisterNotifier(UUID notifierId)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.debug("Removing notifier " + notifierId + " to routing table");
            removeRoutesFromTable(this.routingTable, notifierId);
        }
    }
    
    /**
     * Reregister the given notifier from the route table
     * @param notifier the notifier to reregister
     */
    void reregisterNotifier(NotifierRegistration notifier)
    {
        synchronized (this.routeTableWriteLock)
        {
            logger.debug("Updating notifier " + notifier + " to routing table");
            
        }
    }
    
    public UUID route(UUID siteId, String engine)
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
                return routes[Math.abs(this.random.nextInt() % routes.length)];
            }
        }
        return null;
    }
    
    public Set<String> getAvailableEngines()
    {
        return new HashSet<String>(this.routingTable.keySet());
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        // engine -> site -> notifiers
        for (String engine : this.routingTable.keySet())
        {
            ConcurrentMap<UUID, UUID[]> siteTable = this.routingTable.get(engine);
            for (UUID site : siteTable.keySet())
            {
                UUID[] workers = siteTable.get(site);
                if (workers != null)
                {
                    sb.append(engine).append("::").append(site).append(" => ").append(Arrays.asList(workers)).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
