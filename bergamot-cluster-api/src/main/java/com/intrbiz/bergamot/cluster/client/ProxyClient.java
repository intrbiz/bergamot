package com.intrbiz.bergamot.cluster.client;

import java.util.Set;
import java.util.UUID;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.ProxyConsumer;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;

/**
 * A Bergamot Proxy node client
 */
public interface ProxyClient extends BergamotClient
{
    ProxyConsumer getProxyConsumer();
    
    ProcessorDispatcher getProcessorDispatcher();
    
    WorkerConsumer registerWorker(String host, String application, String info, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws Exception;
    
    void unregisterWorker(UUID workerId) throws Exception;

    void registerAgent(UUID agentId, UUID workerId) throws Exception;
    
    void unregisterAgent(UUID agentId, UUID workerId) throws Exception;
    
    NotificationConsumer registerNotifier(String host, String application, String info, Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws Exception;
    
    void unregisterNotifier(UUID notifierId) throws Exception;
}
