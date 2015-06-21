package com.intrbiz.bergamot.queue;


import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerRequest;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;
import com.intrbiz.bergamot.queue.impl.RabbitBergamotClusterManagerQueue;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.RPCHandler;
import com.intrbiz.queue.RPCServer;
import com.intrbiz.queue.name.RoutingKey;

/**
 * RPC queue to the Bergamot Cluster Manager, allowing the CLI to manage the UI cluster
 */
public abstract class BergamotClusterManagerQueue extends QueueAdapter
{    
    static
    {
        RabbitBergamotClusterManagerQueue.register();
    }
    
    public static BergamotClusterManagerQueue open()
    {
        return QueueManager.getInstance().queueAdapter(BergamotClusterManagerQueue.class);
    }
    
    // RPC methods
    
    public abstract RPCServer<ClusterManagerRequest, ClusterManagerResponse> createBergamotClusterManagerRPCServer(RPCHandler<ClusterManagerRequest, ClusterManagerResponse> handler);
    
    public abstract RPCClient<ClusterManagerRequest, ClusterManagerResponse, RoutingKey> createBergamotClusterManagerRPCClient();
}
