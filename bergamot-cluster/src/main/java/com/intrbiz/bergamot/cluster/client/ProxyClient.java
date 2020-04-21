package com.intrbiz.bergamot.cluster.client;

import java.util.Set;
import java.util.UUID;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.cluster.lookup.ProxyKeyLookup;

/**
 * A Bergamot Proxy node client
 */
public interface ProxyClient extends BergamotClient
{
    ProcessorDispatcher getProcessorDispatcher();
    
    AgentKeyLookup getAgentKeyLookup();
    
    ProxyKeyLookup getProxyKeyLookup();
    
    WorkerConsumer registerWorker(String application, String info, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws KeeperException, InterruptedException;
    
    void unregisterWorker(UUID workerId) throws KeeperException, InterruptedException;

    void registerAgent(UUID agentId) throws KeeperException, InterruptedException;
    
    void unregisterAgent(UUID agentId) throws KeeperException, InterruptedException;
    
    NotificationConsumer registerNotifier(String application, String info, Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws KeeperException, InterruptedException;
    
    void unregisterNotifier(UUID notifierId) throws KeeperException, InterruptedException;
}
