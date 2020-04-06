package com.intrbiz.bergamot.cluster.dispatcher;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.pool.PoolMessage;
import com.intrbiz.bergamot.model.message.pool.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.pool.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.pool.result.ResultMessage;
import com.intrbiz.bergamot.model.message.pool.scheduler.SchedulerMessage;

/**
 * Dispatch results, readings, scheduler actions, agent actions to a processing pool
 */
public class PoolDispatcher
{
    private final HazelcastInstance hazelcast;
    
    private final ConcurrentMap<Integer, IQueue<PoolMessage>> queuesCache = new ConcurrentHashMap<>();
    
    private final SecureRandom random = new SecureRandom();
    
    public PoolDispatcher(HazelcastInstance hazelcast)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
    }
    
    /**
     * Place the given pool message onto the queue for a random processing pool
     * @param message the message
     * @return whether the message was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatch(PoolMessage message)
    {
        return this.dispatch(message.getPool(), message);
    }
    
    /**
     * Place the given pool message onto the queue for the given processing pool
     * @param pool the id of the processing pool, or -1 if not known
     * @param message the message
     * @return whether the message was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatch(int pool, PoolMessage message)
    {
        // Validate the given pool
        int validatedPool = this.validatePool(pool);
        // Get the result queue
        IQueue<PoolMessage> queue = this.getPoolQueue(validatedPool);
        // Offer onto the result queue
        boolean success = queue.offer(message);
        return success ? PublishStatus.Success : PublishStatus.Failed;
    }

    /**
     * Place the given result onto the result queue for a random processing pool
     * @param result the result
     * @return whether the result was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatchResult(ResultMessage result)
    {
        return this.dispatch(result);
    }
    
    /**
     * Place the given result onto the result queue for the given processing pool
     * @param pool the id of the processing pool, or -1 if not known
     * @param result the result
     * @return whether the result was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatchResult(int pool, ResultMessage result)
    {
        return this.dispatch(pool, result);
    }
    
    /**
     * Place the given reading onto the reading queue for a random processing pool
     * @param reading the reading
     * @return whether the reading was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatchReading(ReadingParcelMO reading)
    {
        return this.dispatch(reading);
    }
    
    /**
     * Place the given reading onto the reading queue for the given processing pool
     * @param pool the id of the processing pool, or -1 if not known
     * @param reading the reading
     * @return whether the reading was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatchReading(int pool, ReadingParcelMO reading)
    {
        return this.dispatch(pool, reading);
    }
    
    /**
     * Place the given Agent action onto the queue for the processing pool
     * @param action the Agent action
     * @return whether the Agent action was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed}) 
     */
    public PublishStatus dispatchAgentAction(AgentMessage action)
    {
        return this.dispatch(action);
    }
    
    /**
     * Place the given result onto the result queue for the given processing pool
     * @param pool the id of the processing pool, or -1 if not known
     * @param result the result
     * @return whether the result was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatchSchedulerAction(SchedulerMessage action)
    {
        return this.dispatch(action);
    }
    
    private int validatePool(int pool)
    {
        return (0 <= pool && pool < CheckMO.PROCESSING_POOL_COUNT) ? pool : (this.random.nextInt() % CheckMO.PROCESSING_POOL_COUNT);
    }
    
    private IQueue<PoolMessage> getPoolQueue(int pool)
    {
        return this.queuesCache.computeIfAbsent(pool, (key) -> this.hazelcast.getQueue(HZNames.buildPoolQueueName(key)));
    }
}
