package com.intrbiz.bergamot.queue.impl.hcq;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerResponse;
import com.intrbiz.bergamot.queue.BergamotAgentManagerQueue;
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

public class HCQBergamotAgentManagerQueue extends BergamotAgentManagerQueue
{
    public static final int QUEUE_SIZE = 10;
    
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(BergamotAgentManagerQueue.class, HCQPool.TYPE, HCQBergamotAgentManagerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;

    @SuppressWarnings("unchecked")
    public HCQBergamotAgentManagerQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "bergamot-agent-manager-queue";
    }

    @Override
    public RPCServer<AgentManagerRequest, AgentManagerResponse> createBergamotAgentManagerRPCServer(RPCHandler<AgentManagerRequest, AgentManagerResponse> handler)
    {
        return new HCQRPCServer<AgentManagerRequest, AgentManagerResponse>(
                this.broker, 
                this.transcoder.asQueueEventTranscoder(AgentManagerRequest.class), 
                this.transcoder.asQueueEventTranscoder(AgentManagerResponse.class), 
                handler, 
                new Queue("bergamot.agent.manager.requests", true),
                QUEUE_SIZE,
                new Exchange("bergamot.agent.manager", "fanout", true)
        );
    }
    
    @Override
    public RPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey> createBergamotAgentManagerRPCClient()
    {
        return new HCQRPCClient<AgentManagerRequest, AgentManagerResponse, RoutingKey>(
                this.broker, 
                this.transcoder.asQueueEventTranscoder(AgentManagerRequest.class), 
                this.transcoder.asQueueEventTranscoder(AgentManagerResponse.class), 
                new Exchange("bergamot.agent.manager", "fanout", true),
                new Queue("bergamot.agent.manager.requests", true).toKey(),
                QUEUE_SIZE
        );
    }

    @Override
    public void close()
    {
    }
}
