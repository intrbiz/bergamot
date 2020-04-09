package com.intrbiz.bergamot.cluster.registry;

import static com.intrbiz.bergamot.cluster.util.ZKPaths.*;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * Register Workers in and out of the registry.
 */
public class WorkerRegistar extends GenericRegistar<UUID, WorkerRegistration>
{   
    private static final Logger logger = Logger.getLogger(WorkerRegistar.class);
    
    private final String processorsContainer;
    
    private final ProcessorRouteTable routeTable = new ProcessorRouteTable();
    
    public WorkerRegistar(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, WORKERS);
        this.processorsContainer = zkPath(BERGAMOT, PROCESSORS);
        // Wait for the container to be created
        this.waitForProcessorContainer();
        // Start watching the container
        this.setupProcessorsWatcher();
        // Load the initial list
        this.loadInitialProcessorsList();
    }
    
    private void waitForProcessorContainer() throws KeeperException, InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        Stat stat = this.zooKeeper.exists(this.processorsContainer, (watchedEvent) -> {
            if (watchedEvent.getType() == EventType.NodeCreated)
            {
                latch.countDown();
            }
        });
        // if the node doesn't exist wait for it to be created
        if (stat == null)
        {
            latch.await();
        }
    }
    
    private void setupProcessorsWatcher() throws KeeperException, InterruptedException
    {
        this.zooKeeper.addWatch(this.processorsContainer,  (watchedEvent) -> {
            logger.debug("Processing registry event for " + this.containerPath + ": " + watchedEvent);
            try
            {
                switch (watchedEvent.getType())
                {
                    case NodeCreated:
                        this.routeTable.registerProcessor(uuidPrefixFromName(watchedEvent.getPath()));
                        break;
                    case NodeDeleted:
                        this.routeTable.unregisterProcessor(uuidPrefixFromName(watchedEvent.getPath()));
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e)
            {
                logger.warn("Error processing ZooKeeper event for " + this.processorsContainer, e);
            }
        }, AddWatchMode.PERSISTENT);
    }
    
    private void loadInitialProcessorsList() throws KeeperException, InterruptedException
    {
        for (String path : this.zooKeeper.getChildren(this.processorsContainer, false))
        {
            this.routeTable.registerProcessor(uuidPrefixFromName(path));
        }
    }
    
    public void registerWorker(WorkerRegistration worker) throws KeeperException, InterruptedException
    {
        this.registerItem(worker.getId(), worker);
    }
    
    public void reregisterWorker(WorkerRegistration worker) throws KeeperException, InterruptedException
    {
        this.reregisterItem(worker.getId(), worker);
    }
    
    public void unregisterWorker(UUID workerId) throws KeeperException, InterruptedException
    {
        this.unregisterItem(workerId);
    }
    
    /**
     * Get the Processors routing table
     * @return
     */
    public ProcessorRouteTable getRouteTable()
    {
        return this.routeTable;
    }
}
