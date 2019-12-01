package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.coordinator.WorkerCoordinator;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

public class WorkerProducer
{
    /**
     * The hazelcast instance to use
     */
    private final HazelcastInstance hazelcast;
    
    private final WorkerCoordinator coordinator;
    
    /**
     * Our cache of worker queues
     */
    private final ConcurrentMap<UUID, IQueue<ExecuteCheck>> workerQueues;
    
    public WorkerProducer(HazelcastInstance hazelcast, WorkerCoordinator coordinator)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.coordinator = Objects.requireNonNull(coordinator);
        this.workerQueues = new ConcurrentHashMap<>();
    }
    
    private IQueue<ExecuteCheck> getCheckQueue(UUID workerId)
    {
        return this.workerQueues.computeIfAbsent(workerId, (key) -> {
            return this.hazelcast.getQueue(ObjectNames.buildCheckQueueName(key));
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
    
    public static enum PublishStatus
    {
        Success,
        Failed,
        Unroutable
    }
}
