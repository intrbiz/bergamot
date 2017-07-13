package com.intrbiz.bergamot.queue.impl.hcq;

import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerRequest;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;
import com.intrbiz.bergamot.queue.BergamotClusterManagerQueue;
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

public class HCQBergamotClusterManagerQueue extends BergamotClusterManagerQueue
{
    public static final void register()
    {
        QueueManager.getInstance().registerQueueAdapter(BergamotClusterManagerQueue.class, HCQPool.TYPE, HCQBergamotClusterManagerQueue::new);
    }

    private final BergamotTranscoder transcoder = new BergamotTranscoder();

    private final QueueBrokerPool<HCQClient> broker;

    @SuppressWarnings("unchecked")
    public HCQBergamotClusterManagerQueue(QueueBrokerPool<?> broker)
    {
        this.broker = (QueueBrokerPool<HCQClient>) broker;
    }

    public String getName()
    {
        return "bergamot-cluster-manager-queue";
    }

    @Override
    public RPCServer<ClusterManagerRequest, ClusterManagerResponse> createBergamotClusterManagerRPCServer(RPCHandler<ClusterManagerRequest, ClusterManagerResponse> handler)
    {
        return new HCQRPCServer<ClusterManagerRequest, ClusterManagerResponse>(
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
        return new HCQRPCClient<ClusterManagerRequest, ClusterManagerResponse, RoutingKey>(
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
