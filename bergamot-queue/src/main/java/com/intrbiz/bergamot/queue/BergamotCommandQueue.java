package com.intrbiz.bergamot.queue;


import com.intrbiz.bergamot.model.message.command.CommandRequest;
import com.intrbiz.bergamot.model.message.command.CommandResponse;
import com.intrbiz.bergamot.queue.impl.RabbitBergamotCommandQueue;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.RPCHandler;
import com.intrbiz.queue.RPCServer;
import com.intrbiz.queue.name.RoutingKey;

/**
 * RPC queue for workers or other daemons to exeucte commands against the core
 * 
 */
public abstract class BergamotCommandQueue extends QueueAdapter
{    
    static
    {
        RabbitBergamotCommandQueue.register();
    }
    
    public static BergamotCommandQueue open()
    {
        return QueueManager.getInstance().queueAdapter(BergamotCommandQueue.class);
    }
    
    // RPC methods
    
    public abstract RPCServer<CommandRequest, CommandResponse> createBergamotCommandRPCServer(RPCHandler<CommandRequest, CommandResponse> handler);
    
    public abstract RPCClient<CommandRequest, CommandResponse, RoutingKey> createBergamotCommandRPCClient();
}
