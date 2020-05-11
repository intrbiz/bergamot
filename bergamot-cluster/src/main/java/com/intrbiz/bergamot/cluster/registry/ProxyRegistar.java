package com.intrbiz.bergamot.cluster.registry;

import static com.intrbiz.bergamot.cluster.util.ZKPaths.*;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.model.message.cluster.ProxyRegistration;

/**
 * Register Proxy in and out of the registry.
 */
public class ProxyRegistar extends GenericRegistar<UUID, ProxyRegistration>
{
    public ProxyRegistar(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, PROXIES);
    }
    
    public void registerProxy(ProxyRegistration proxy) throws KeeperException, InterruptedException
    {
        this.registerItem(proxy.getId(), proxy);
    }
    
    public void reregisterProxy(ProxyRegistration proxy) throws KeeperException, InterruptedException
    {
        this.reregisterItem(proxy.getId(), proxy);
    }
    
    public void unregisterProxy(UUID proxyId) throws KeeperException, InterruptedException
    {
        this.unregisterItem(proxyId);
    }

}
