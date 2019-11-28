package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.coordinator.ClusterNames;
import com.intrbiz.bergamot.cluster.coordinator.WorkerSchedulerCoordinator;
import com.intrbiz.bergamot.model.message.result.ResultMO;

public class ResultConsumer implements com.intrbiz.bergamot.result.ResultConsumer
{       
    /**
     * The hazelcast instance to use
     */
    private final HazelcastInstance hazelcast;
    
    private final WorkerSchedulerCoordinator coordinator;
    
    private final UUID poolId;
    
    private final IQueue<ResultMO> resultQueue;
    
    public ResultConsumer(HazelcastInstance hazelcast, UUID processingPool, WorkerSchedulerCoordinator coordinator)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.poolId = Objects.requireNonNull(processingPool);
        this.coordinator = Objects.requireNonNull(coordinator);
        // Create our queues
        this.resultQueue = this.hazelcast.getQueue(ClusterNames.buildResultQueueName(this.poolId));
    }
    
    public ResultMO pollResult()
    {
        try
        {
            return this.resultQueue.poll(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException ie)
        {
            // ignore
        }
        return null;
    }
}
