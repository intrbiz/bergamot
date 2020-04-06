package com.intrbiz.bergamot.cluster.registry;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;

/**
 * Register Notifiers in and out of the registry.
 */
public class NotifierRegistar extends GenericRegistar<UUID, NotifierRegistration>
{    
    public NotifierRegistar(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, ZKPaths.NOTIFIERS);
    }
    
    public void registerNotifier(NotifierRegistration notifier) throws KeeperException, InterruptedException
    {
        this.registerItem(notifier.getId(), notifier);
    }
    
    public void reregisterNotifier(NotifierRegistration notifier) throws KeeperException, InterruptedException
    {
        this.reregisterItem(notifier.getId(), notifier);
    }
    
    public void unregisterNotifier(UUID notifierId) throws KeeperException, InterruptedException
    {
        this.unregisterItem(notifierId);
    }
}
