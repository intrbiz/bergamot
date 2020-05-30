package com.intrbiz.bergamot.cluster.registry;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;

/**
 * Register Processors in and out of the registry.
 */
public class ProcessorRegistar extends GenericRegistar<UUID, ProcessorRegistration>
{    
    public ProcessorRegistar(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, ProcessorRegistration.class, ZKPaths.PROCESSORS);
    }
    
    public void registerProcessor(ProcessorRegistration processor) throws KeeperException, InterruptedException
    {
        this.registerItem(processor.getId(), processor);
    }
    
    public void unregisterProcessor(UUID processorId) throws KeeperException, InterruptedException
    {
        this.unregisterItem(processorId);
    }
}
