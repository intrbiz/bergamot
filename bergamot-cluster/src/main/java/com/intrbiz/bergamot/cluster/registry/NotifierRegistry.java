package com.intrbiz.bergamot.cluster.registry;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;

/**
 * A registry of Bergamot Notifiers
 */
public class NotifierRegistry extends GenericRegistry<UUID, NotifierRegistration>
{
    public static final Logger logger = Logger.getLogger(NotifierRegistry.class);
    
    private final NotifierRouteTable routeTable = new NotifierRouteTable();
    
    public NotifierRegistry(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, NotifierRegistration.class, UUID::fromString, ZKPaths.NOTIFIERS);
        // Init the route table
        this.init();
    }
    
    protected void init() throws KeeperException, InterruptedException
    {
        Set<NotifierRegistration> notifiers = this.getNotifiers();
        logger.info("Initialising with notifiers: " + notifiers);
        this.routeTable.registerNotifiers(notifiers);
        logger.info("Notifier routing table:\n" + this.routeTable);
    }
    
    protected void onItemAdded(UUID id, NotifierRegistration item)
    {
        logger.info("Adding notifier: " + item);
        this.routeTable.registerNotifier(item);
        logger.info("Updated Notifier routing table:\n" + this.routeTable);
    }
    
    protected void onItemRemoved(UUID id)
    {
        logger.info("Removing notifier: " + id);
        this.routeTable.unregisterNotifier(id);
        logger.info("Updated Worker routing table:\n" + this.routeTable);
    }
    
    protected void onItemUpdated(UUID id, NotifierRegistration item)
    {
        logger.info("Updating notifier: " + item);
        this.routeTable.reregisterNotifier(item);
        logger.info("Updated Notifier routing table:\n" + this.routeTable);
    }

    /**
     * Get the Notifier routing table
     * @return
     */
    public NotifierRouteTable getRouteTable()
    {
        return this.routeTable;
    }
    
    /**
     * Get the registration data for a specific Notifier from ZooKeeper
     * @param notifierId the notifier id
     * @return the Notifier registration data or null
     * @throws KeeperException
     * @throws InterruptedException
     */
    public NotifierRegistration getNotifier(UUID notifierId) throws KeeperException, InterruptedException
    {
        return this.getItem(notifierId);
    }
    
    /**
     * Get the current list of Notifiers from ZooKeeper 
     * @return The list of currently registered Workers
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Set<NotifierRegistration> getNotifiers() throws KeeperException, InterruptedException
    {
        return this.getItems();
    }
    
}
