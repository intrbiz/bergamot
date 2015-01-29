package com.intrbiz.bergamot.queue;


import java.util.UUID;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.queue.impl.RabbitWorkerQueue;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.queue.key.WorkerKey;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.NullKey;

/**
 * Send events to and from compute hosts
 */
public abstract class WorkerQueue extends QueueAdapter
{    
    static
    {
        RabbitWorkerQueue.register();
    }
    
    public static WorkerQueue open()
    {
        return QueueManager.getInstance().queueAdapter(WorkerQueue.class);
    }
    
    // checks
    
    public abstract RoutedProducer<ExecuteCheck, WorkerKey> publishChecks(WorkerKey defaultKey);
    
    public RoutedProducer<ExecuteCheck, WorkerKey> publishChecks()
    {
        return this.publishChecks(null);
    }
    
    public abstract Consumer<ExecuteCheck, WorkerKey> consumeChecks(DeliveryHandler<ExecuteCheck> handler, UUID site, String workerPool, String engine);
    
    public abstract Consumer<ExecuteCheck, NullKey> consumeDeadChecks(DeliveryHandler<ExecuteCheck> handler);
    
    // result
    
    public abstract RoutedProducer<Result, ResultKey> publishResults(ResultKey defaultKey);
    
    public RoutedProducer<Result, ResultKey> publishResults()
    {
        return this.publishResults(null);
    }
    
    /**
     * Consume results targeted to a specific processor
     */
    public abstract Consumer<Result, ResultKey> consumeResults(DeliveryHandler<Result> handler, String instance);
    
    /**
     * Consume results which were not successfully routed to the intended processor
     * @return
     */
    public abstract Consumer<Result, ResultKey> consumeFallbackResults(DeliveryHandler<Result> handler);
}
