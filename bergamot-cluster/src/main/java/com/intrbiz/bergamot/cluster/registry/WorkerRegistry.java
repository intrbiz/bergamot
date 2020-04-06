package com.intrbiz.bergamot.cluster.registry;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * A registry of Bergamot Workers
 */
public class WorkerRegistry extends GenericRegistry<UUID, WorkerRegistration>
{
    public static final Logger logger = Logger.getLogger(WorkerRegistry.class);
    
    private final WorkerRouteTable routeTable = new WorkerRouteTable();
    
    public WorkerRegistry(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, WorkerRegistration.class, UUID::fromString, ZKPaths.WORKERS);
        // Init the route table
        this.init();
    }
    
    protected void init() throws KeeperException, InterruptedException
    {
        Set<WorkerRegistration> workers = this.getWorkers();
        logger.info("Initialising with workers: " + workers);
        this.routeTable.registerWorkers(workers);
        logger.info("Worker routing table:\n" + this.routeTable);
    }
    
    protected void onItemAdded(UUID id, WorkerRegistration item)
    {
        logger.info("Adding worker: " + item);
        this.routeTable.registerWorker(item);
        logger.info("Updated Worker routing table:\n" + this.routeTable);
    }
    
    protected void onItemRemoved(UUID id)
    {
        logger.info("Removing worker: " + id);
        this.routeTable.unregisterWorker(id);
        logger.info("Updated Worker routing table:\n" + this.routeTable);
    }
    
    protected void onItemUpdated(UUID id, WorkerRegistration item)
    {
        logger.info("Updating worker: " + item);
        this.routeTable.reregisterWorker(item);
        logger.info("Updated Worker routing table:\n" + this.routeTable);
    }
    
    /**
     * Get the Worker routing table
     * @return
     */
    public WorkerRouteTable getRouteTable()
    {
        return this.routeTable;
    }
    
    /**
     * Get the registration data for a specific Worker from ZooKeeper
     * @param workerId the worker id
     * @return the Worker registration data or null
     * @throws KeeperException
     * @throws InterruptedException
     */
    public WorkerRegistration getWorker(UUID workerId) throws KeeperException, InterruptedException
    {
        return this.getItem(workerId);
    }
    
    /**
     * Get the current list of Workers from ZooKeeper 
     * @return The list of currently registered Workers
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Set<WorkerRegistration> getWorkers() throws KeeperException, InterruptedException
    {
        return this.getItems();
    }
}
