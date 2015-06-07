package com.intrbiz.bergamot.queue;


import java.util.UUID;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.impl.RabbitWorkerQueue;
import com.intrbiz.bergamot.queue.key.ReadingKey;
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
    
    public abstract Consumer<ExecuteCheck, WorkerKey> consumeChecks(DeliveryHandler<ExecuteCheck> handler, UUID site, String workerPool, String engine, boolean agentRouting, UUID workerId);
    
    public Consumer<ExecuteCheck, WorkerKey> consumeChecks(DeliveryHandler<ExecuteCheck> handler, UUID site, String workerPool, String engine)
    {
        return this.consumeChecks(handler, site, workerPool, engine, false, null);
    }
    
    public abstract Consumer<ExecuteCheck, NullKey> consumeDeadChecks(DeliveryHandler<ExecuteCheck> handler);
    
    public abstract Consumer<ExecuteCheck, NullKey> consumeDeadAgentChecks(DeliveryHandler<ExecuteCheck> handler);
    
    // result
    
    public abstract RoutedProducer<ResultMO, ResultKey> publishResults(ResultKey defaultKey);
    
    public RoutedProducer<ResultMO, ResultKey> publishResults()
    {
        return this.publishResults(null);
    }
    
    /**
     * Consume results targeted to a specific processor
     */
    public abstract Consumer<ResultMO, ResultKey> consumeResults(DeliveryHandler<ResultMO> handler, String instance);
    
    /**
     * Consume results which were not successfully routed to the intended processor
     * @return
     */
    public abstract Consumer<ResultMO, ResultKey> consumeFallbackResults(DeliveryHandler<ResultMO> handler);
    
    // metrics
    
    public abstract RoutedProducer<ReadingParcelMO, ReadingKey> publishReadings(ReadingKey defaultKey);
    
    public RoutedProducer<ReadingParcelMO, ReadingKey> publishReadings()
    {
        return this.publishReadings(null);
    }
    
    /**
     * Consume readings targeted to a specific processor
     */
    public abstract Consumer<ReadingParcelMO, ReadingKey> consumeReadings(DeliveryHandler<ReadingParcelMO> handler, String instance);
    
    /**
     * Consume readings which were not successfully routed to the intended processor
     * @return
     */
    public abstract Consumer<ReadingParcelMO, ReadingKey> consumeFallbackReadings(DeliveryHandler<ReadingParcelMO> handler);
}
