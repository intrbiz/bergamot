package com.intrbiz.bergamot.cluster.dispatcher;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerMessage;

/**
 * Dispatch scheduler actions  a scheduling pool
 */
public class SchedulingPoolDispatcher
{
    private final HazelcastInstance hazelcast;
    
    private final ConcurrentMap<Integer, IQueue<SchedulerMessage>> queuesCache = new ConcurrentHashMap<>();
    
    public SchedulingPoolDispatcher(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
    }
    
    /**
     * Place the given scheduler message onto the queue for the given scheduling pool
     * @param pool the id of the scheduling pool
     * @param message the message
     * @return whether the message was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatch(int pool, SchedulerMessage message)
    {
        // Get the result queue
        IQueue<SchedulerMessage> queue = this.getSchedulingPoolQueue(pool);
        // Offer onto the result queue
        boolean success = queue.offer(message);
        return success ? PublishStatus.Success : PublishStatus.Failed;
    }
    
    private IQueue<SchedulerMessage> getSchedulingPoolQueue(int pool)
    {
        return this.queuesCache.computeIfAbsent(pool, (key) -> this.hazelcast.getQueue(HZNames.buildSchedulingPoolQueueName(key)));
    }
}
