package com.intrbiz.bergamot.queue.impl;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
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

public class RabbitBergamotAgentManagerQueue extends BergamotAgentManagerQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(BergamotAgentManagerQueue.class, RabbitBergamotAgentManagerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;

    @SuppressWarnings("unchecked")
    public RabbitBergamotAgentManagerQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "bergamot-agent-manager-queue";
    }

    @Override
    public RPCServer<AgentManagerRequest, AgentManagerResponse> createBergamotAgentManagerRPCServer(RPCHandler<AgentManagerRequest, AgentManagerResponse> handler)
    {
        return new RabbitRPCServer<AgentManagerRequest, AgentManagerResponse>(
                this.broker, 
                this.transcoder.asQueueEventTranscoder(AgentManagerRequest.class), 
                this.transcoder.asQueueEventTranscoder(AgentManagerResponse.class), 
                handler, 
                new Queue("bergamot.agent.manager.requests", true), 
                new Exchange("bergamot.agent.manager", "fanout", true)
        );
    }
    
    @Override
    public RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> createBergamotAgentManagerRPCClient()
    {
        return new RabbitRPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey>(
                this.broker, 
                this.transcoder.asQueueEventTranscoder(AgentManagerRequest.class), 
                this.transcoder.asQueueEventTranscoder(AgentManagerResponse.class), 
                new Exchange("bergamot.agent.manager", "fanout", true),
                new Queue("bergamot.agent.manager.requests", true).toKey()
        );
    }

    @Override
    public void close()
    {
    }
}
