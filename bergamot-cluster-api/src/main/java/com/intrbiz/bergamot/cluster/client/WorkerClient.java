package com.intrbiz.bergamot.cluster.client;

import java.util.UUID;

import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;

/**
 * A Bergamot Worker node client
 */
public interface WorkerClient extends BergamotClient
{   
    WorkerConsumer getWorkerConsumer();

    ProcessorDispatcher getProcessorDispatcher();

    void registerAgent(UUID agentId) throws Exception;
    
    void unregisterAgent(UUID agentId) throws Exception;
}
