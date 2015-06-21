package com.intrbiz.bergamot.queue.impl;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerRequest;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;
import com.intrbiz.bergamot.queue.BergamotClusterManagerQueue;
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

public class RabbitBergamotClusterManagerQueue extends BergamotClusterManagerQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(BergamotClusterManagerQueue.class, RabbitBergamotClusterManagerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<Channel> broker;

    @SuppressWarnings("unchecked")
    public RabbitBergamotClusterManagerQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<Channel>) broker;
    }

    public String getName()
    {
        return "bergamot-cluster-manager-queue";
    }

    @Override
    public RPCServer<ClusterManagerRequest, ClusterManagerResponse> createBergamotClusterManagerRPCServer(RPCHandler<ClusterManagerRequest, ClusterManagerResponse> handler)
    {
        return new RabbitRPCServer<ClusterManagerRequest, ClusterManagerResponse>(
                this.broker, 
                this.transcoder.asQueueEventTranscoder(ClusterManagerRequest.class), 
                this.transcoder.asQueueEventTranscoder(ClusterManagerResponse.class), 
                handler, 
                new Queue("bergamot.cluster.manager.requests", true), 
                new Exchange("bergamot.cluster.manager", "fanout", true)
        );
    }
    
    @Override
    public RPCClient<ClusterManagerRequest, ClusterManagerResponse, RoutingKey> createBergamotClusterManagerRPCClient()
    {
        return new RabbitRPCClient<ClusterManagerRequest, ClusterManagerResponse, RoutingKey>(
                this.broker, 
                this.transcoder.asQueueEventTranscoder(ClusterManagerRequest.class), 
                this.transcoder.asQueueEventTranscoder(ClusterManagerResponse.class), 
                new Exchange("bergamot.cluster.manager", "fanout", true),
                new Queue("bergamot.cluster.manager.requests", true).toKey()
        );
    }

    @Override
    public void close()
    {
    }
}
