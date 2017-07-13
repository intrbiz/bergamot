package com.intrbiz.bergamot.queue.impl.hcq;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.command.CommandRequest;
import com.intrbiz.bergamot.model.message.command.CommandResponse;
import com.intrbiz.bergamot.queue.BergamotCommandQueue;
import com.intrbiz.hcq.client.HCQClient;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.RPCHandler;
import com.intrbiz.queue.RPCServer;
import com.intrbiz.queue.hcq.HCQPool;
import com.intrbiz.queue.hcq.HCQRPCClient;
import com.intrbiz.queue.hcq.HCQRPCServer;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;

public class HCQBergamotCommandQueue extends BergamotCommandQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(BergamotCommandQueue.class, HCQPool.TYPE, HCQBergamotCommandQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;

    @SuppressWarnings("unchecked")
    public HCQBergamotCommandQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "bergamot-agent-manager-queue";
    }

    @Override
    public RPCServer<CommandRequest, CommandResponse> createBergamotCommandRPCServer(RPCHandler<CommandRequest, CommandResponse> handler)
    {
        return new HCQRPCServer<CommandRequest, CommandResponse>(
                this.broker, 
                this.transcoder.asQueueEventTranscoder(CommandRequest.class), 
                this.transcoder.asQueueEventTranscoder(CommandResponse.class), 
                handler, 
                new Queue("bergamot.command.requests", true), 
                new Exchange("bergamot.command", "fanout", true)
        );
    }
    
    @Override
    public RPCClient<CommandRequest, CommandResponse, RoutingKey> createBergamotCommandRPCClient()
    {
        return new HCQRPCClient<CommandRequest, CommandResponse, RoutingKey>(
                this.broker, 
                this.transcoder.asQueueEventTranscoder(CommandRequest.class), 
                this.transcoder.asQueueEventTranscoder(CommandResponse.class), 
                new Exchange("bergamot.command", "fanout", true),
                new Queue("bergamot.command.requests", true).toKey()
        );
    }

    @Override
    public void close()
    {
    }
}
