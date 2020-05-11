package com.intrbiz.bergamot.cluster.registry;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.ProxyRegistration;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * A registry of Bergamot Proxy nodes
 */
public class ProxyRegistry extends GenericRegistry<UUID, ProxyRegistration>
{
    public static final Logger logger = Logger.getLogger(ProxyRegistry.class);
    
    private final ProxyRouteTable routeTable = new ProxyRouteTable();
    
    public ProxyRegistry(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, ProxyRegistration.class, UUID::fromString, ZKPaths.PROXIES);
        // Init the route table
        this.init();
    }
    
    protected void init() throws KeeperException, InterruptedException
    {
        Set<UUID> poxies = this.getProxies().stream().map(ProxyRegistration::getId).collect(Collectors.toSet());
        logger.info("Initialising with proxies: " + poxies);
        this.routeTable.registerProxies(poxies);
        logger.info("Proxy routing table:\n" + this.routeTable);
    }
    
    @Override
    protected void onConnect()
    {
        try
        {
            this.init();
        }
        catch (KeeperException | InterruptedException e)
        {
            logger.error("");
        }
    }
    
    protected void onItemAdded(UUID id, WorkerRegistration item)
    {
        logger.info("Adding proxy: " + item);
        this.routeTable.registerProxy(id);
        logger.info("Updated Proxy routing table:\n" + this.routeTable);
    }
    
    protected void onItemRemoved(UUID id)
    {
        logger.info("Removing proxy: " + id);
        this.routeTable.unregisterProxy(id);
        logger.info("Updated Proxy routing table:\n" + this.routeTable);
    }
    
    protected void onItemUpdated(UUID id, WorkerRegistration item)
    {
        logger.info("Updating proxy: " + item);
        this.routeTable.registerProxy(id);
        logger.info("Updated Proxy routing table:\n" + this.routeTable);
    }
    
    /**
     * Get the Proxy routing table
     * @return
     */
    public ProxyRouteTable getRouteTable()
    {
        return this.routeTable;
    }
    
    /**
     * Get the registration data for a specific Worker from ZooKeeper
     * @param proxyId the proxy id
     * @return the Proxy registration data or null
     * @throws KeeperException
     * @throws InterruptedException
     */
    public ProxyRegistration getProxy(UUID proxyId) throws KeeperException, InterruptedException
    {
        return this.getItem(proxyId);
    }
    
    /**
     * Get the current list of Proxies from ZooKeeper 
     * @return The list of currently registered Proxies
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Set<ProxyRegistration> getProxies() throws KeeperException, InterruptedException
    {
        return this.getItems();
    }
}
