package com.intrbiz.bergamot.cluster.registry;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class ProxyRouteTable
{
    private final ConcurrentMap<UUID, UUID> proxyMap = new ConcurrentHashMap<>();
    
    private final SecureRandom random = new SecureRandom();
    
    private volatile UUID[] proxyTable = new UUID[0];
    
    public ProxyRouteTable()
    {
        super();
    }
    
    void registerProxies(Set<UUID> proxies)
    {
        this.proxyMap.clear();
        for (UUID proxy : proxies)
        {
            this.proxyMap.put(proxy, proxy);
        }
        this.updateProxies();
    }
    
    void registerProxy(UUID proxy)
    {
        this.proxyMap.put(proxy, proxy);
        this.updateProxies();
    }
    
    void unregisterProxy(UUID proxy)
    {
        this.proxyMap.remove(proxy);
        this.updateProxies();
    }
    
    private void updateProxies()
    {
        this.proxyTable = this.proxyMap.keySet().stream()
                            .sorted()
                            .collect(Collectors.toList())
                            .toArray(new UUID[0]);
    }
    
    public boolean hasProxy(UUID id)
    {
        return this.proxyMap.containsKey(id);
    }
    
    /**
     * Get all available processors
     * @return the processor ids
     */
    public UUID[] getProxies()
    {
        return this.proxyTable;
    }
    
    /**
     * Select a proxy at random
     * @return a proxy id or null
     */
    public UUID routeProxy()
    {
        UUID[] procs = this.proxyTable;
        return procs.length > 0 ? procs[Math.abs(this.random.nextInt() % procs.length)] : null;
    }
    
    public String toString()
    {
        return Arrays.toString(this.proxyTable);
    }
}
