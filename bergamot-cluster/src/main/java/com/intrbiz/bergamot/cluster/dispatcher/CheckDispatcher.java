package com.intrbiz.bergamot.cluster.dispatcher;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.collection.IQueue;
import com.hazelcast.collection.impl.queue.QueueService;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.registry.AgentRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRouteTable;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.pool.check.ExecuteCheck;

/**
 * Dispatch checks to workers
 */
public class CheckDispatcher
{
    private final WorkerRegistry workers;
    
    private final WorkerRouteTable workerRouteTable;
    
    private final AgentRegistry agents;
    
    private final HazelcastInstance hazelcast;
    
    private final ConcurrentMap<String, IQueue<ExecuteCheck>> queuesCache = new ConcurrentHashMap<>();
    
    public CheckDispatcher(WorkerRegistry workers, AgentRegistry agents, HazelcastInstance hazelcast)
    {
        super();
        this.workers = Objects.requireNonNull(workers);
        this.workerRouteTable = Objects.requireNonNull(this.workers.getRouteTable());
        this.agents = Objects.requireNonNull(agents);
        this.hazelcast = Objects.requireNonNull(hazelcast);
        // Setup a object listener to clean up our queue cache
        this.hazelcast.addDistributedObjectListener(new QueueListener());
    }
    
    public PublishStatus dispatchCheck(ExecuteCheck check)
    {
        // Pick a worker for this check
        UUID workerId = this.routeCheck(check);
        if (workerId == null) return PublishStatus.Unroutable;
        // Get the worker queue
        IQueue<ExecuteCheck> queue = this.getCheckQueue(workerId);
        // Offer onto the worker queue
        boolean success = queue.offer(check);
        return success ? PublishStatus.Success : PublishStatus.Failed;

    }
    
    protected UUID routeCheck(ExecuteCheck check)
    {
        if (check.getAgentId() == null)
        {
            return this.agents.routeAgent(check.getAgentId());
        }
        else
        {
            return this.workerRouteTable.route(check.getSiteId(), check.getWorkerPool(), check.getEngine());
        }
    }
    
    private IQueue<ExecuteCheck> getCheckQueue(UUID workerId)
    {
        return this.queuesCache.computeIfAbsent(HZNames.buildWorkerQueueName(workerId), this.hazelcast::getQueue);
    }

    private class QueueListener implements DistributedObjectListener
    {
        @Override
        public void distributedObjectCreated(DistributedObjectEvent event)
        {
        }

        @Override
        public void distributedObjectDestroyed(DistributedObjectEvent event)
        {
            if (QueueService.SERVICE_NAME.equals(event.getServiceName()))
            {
                CheckDispatcher.this.queuesCache.remove(event.getObjectName());
            }
        }
    }
}
