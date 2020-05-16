package com.intrbiz.bergamot.cluster.registry;

import static com.intrbiz.bergamot.cluster.util.ZKPaths.*;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * Register Workers in and out of the registry.
 */
public class WorkerRegistar extends GenericRegistar<UUID, WorkerRegistration>
{
    public WorkerRegistar(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, WORKERS);
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
}
