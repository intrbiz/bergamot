package com.intrbiz.bergamot.queue;


import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.queue.impl.rabbit.RabbitBergamotAgentManagerQueue;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.RPCHandler;
import com.intrbiz.queue.RPCServer;
import com.intrbiz.queue.name.RoutingKey;

/**
 * RPC queue to the Bergamot Agent Manager certificate manager
 * 
 */
public abstract class BergamotAgentManagerQueue extends QueueAdapter
{    
    static
    {
        RabbitBergamotAgentManagerQueue.register();
    }
    
    public static BergamotAgentManagerQueue open()
    {
        return QueueManager.getInstance().queueAdapter(BergamotAgentManagerQueue.class);
    }
    
    // RPC methods
    
    public abstract RPCServer<AgentManagerRequest, AgentManagerResponse> createBergamotAgentManagerRPCServer(RPCHandler<AgentManagerRequest, AgentManagerResponse> handler);
    
    public abstract RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> createBergamotAgentManagerRPCClient();
}
