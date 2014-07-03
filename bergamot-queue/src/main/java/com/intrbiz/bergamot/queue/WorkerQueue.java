package com.intrbiz.bergamot.queue;


import java.util.UUID;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.bergamot.queue.impl.RabbitWorkerQueue;
import com.intrbiz.queue.Consumer;
import com.intrbiz.queue.DeliveryHandler;
import com.intrbiz.queue.QueueAdapter;
import com.intrbiz.queue.QueueManager;
import com.intrbiz.queue.RoutedProducer;
import com.intrbiz.queue.name.GenericKey;

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
    
    public abstract RoutedProducer<ExecuteCheck> publishChecks(GenericKey defaultKey);
    
    public RoutedProducer<ExecuteCheck> publishChecks()
    {
        return this.publishChecks(null);
    }
    
    public abstract Consumer<ExecuteCheck> consumeChecks(DeliveryHandler<ExecuteCheck> handler, UUID site, String workerPool, String engine);
    
    public abstract Consumer<ExecuteCheck> consumeDeadChecks(DeliveryHandler<ExecuteCheck> handler);
    
    // result
    
    public abstract RoutedProducer<Result> publishResults(GenericKey defaultKey);
    
    public RoutedProducer<Result> publishResults()
    {
        return this.publishResults(null);
    }
    
    /**
     * Consume results targeted to a specific processor
     */
    public abstract Consumer<Result> consumeResults(DeliveryHandler<Result> handler, String instance);
    
    /**
     * Consume results which were not successfully routed to the intended processor
     * @return
     */
    public abstract Consumer<Result> consumeFallbackResults(DeliveryHandler<Result> handler);
}
