package com.intrbiz.bergamot.cluster.client;

import java.util.UUID;

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

    public void registerAgent(UUID agentId) throws Exception;
    
    public void unregisterAgent(UUID agentId) throws Exception;
}
