package com.intrbiz.bergamot.queue.impl;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.command.CommandRequest;
import com.intrbiz.bergamot.model.message.command.CommandResponse;
import com.intrbiz.bergamot.queue.BergamotCommandQueue;
import com.intrbiz.queue.QueueBrokerPool;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RPCClient;
import com.intrbiz.queue.RPCHandler;
import com.intrbiz.queue.RPCServer;
import com.intrbiz.queue.name.Exchange;
import com.intrbiz.queue.name.Queue;
import com.intrbiz.queue.name.RoutingKey;
import com.intrbiz.queue.rabbit.RabbitRPCClient;
import com.intrbiz.queue.rabbit.RabbitRPCServer;
import com.rabbitmq.client.Channel;

public class RabbitBergamotCommandQueue extends BergamotCommandQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(BergamotCommandQueue.class, RabbitBergamotCommandQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;

    @SuppressWarnings("unchecked")
    public RabbitBergamotCommandQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "bergamot-agent-manager-queue";
    }

    @Override
    public RPCServer<CommandRequest, CommandResponse> createBergamotCommandRPCServer(RPCHandler<CommandRequest, CommandResponse> handler)
    {
        return new RabbitRPCServer<CommandRequest, CommandResponse>(
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
        return new RabbitRPCClient<CommandRequest, CommandResponse, RoutingKey>(
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
