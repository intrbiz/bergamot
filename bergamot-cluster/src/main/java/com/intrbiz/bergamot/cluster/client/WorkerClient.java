package com.intrbiz.bergamot.cluster.client;

import java.util.Set;
import java.util.UUID;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;

/**
 * A Bergamot Worker node client
 */
public interface WorkerClient extends BergamotClient
{   
    public WorkerConsumer getWorkerConsumer();

    public ProcessorDispatcher getProcessorDispatcher();
    
    public AgentKeyLookup getAgentKeyLookup();
    
    public void registerWorker(Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws KeeperException, InterruptedException;
    
    public void unregisterWorker() throws KeeperException, InterruptedException;

    public void registerAgent(UUID agentId) throws KeeperException, InterruptedException;
    
    public void unregisterAgent(UUID agentId) throws KeeperException, InterruptedException;
}
