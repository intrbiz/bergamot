package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.coordinator.ClusterNames;
import com.intrbiz.bergamot.cluster.coordinator.WorkerSchedulerCoordinator;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.scheduler.CheckProducer;

public class WorkerProducer implements CheckProducer
{    
    /**
     * The hazelcast instance to use
     */
    private final HazelcastInstance hazelcast;
    
    private final WorkerSchedulerCoordinator coordinator;
    
    private final UUID poolId;
    
    /**
     * Our cache of worker queues
     */
    private final ConcurrentMap<UUID, IQueue<ExecuteCheck>> workerQueues;
    
    private final IQueue<ResultMO> resultQueue;
    
    public WorkerProducer(HazelcastInstance hazelcast, UUID poolId, WorkerSchedulerCoordinator coordinator)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.coordinator = Objects.requireNonNull(coordinator);
        this.poolId = Objects.requireNonNull(poolId);
        this.workerQueues = new ConcurrentHashMap<>();
        this.resultQueue = this.hazelcast.getQueue(ClusterNames.buildResultQueueName(this.poolId));
    }
    
    public UUID getPoolId()
    {
        return this.poolId;
    }
    
    private IQueue<ExecuteCheck> getCheckQueue(UUID workerId)
    {
        return this.workerQueues.computeIfAbsent(workerId, (key) -> {
            return this.hazelcast.getQueue(ClusterNames.buildCheckQueueName(key));
        });
    }
    
    public PublishStatus publishCheck(ExecuteCheck check)
    {
        // Pick a worker for this check
        UUID workerId = this.coordinator.routeCheck(check);
        if (workerId == null) return PublishStatus.Unroutable;
        // Get the worker queue
        IQueue<ExecuteCheck> queue = this.getCheckQueue(workerId);
        // Offer onto the worker queue
        boolean success = queue.offer(check);
        return success ? PublishStatus.Success : PublishStatus.Failed;
    }
    
    public boolean publishFailedCheck(ResultMO result)
    {
        return this.resultQueue.offer(result);
    }
}
