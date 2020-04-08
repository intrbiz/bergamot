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
import com.intrbiz.bergamot.cluster.registry.ProcessorRouteTable;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.processor.ProcessorHashable;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;

/**
 * Dispatch results, readings, agent message to a processor
 */
public class ProcessorDispatcher
{
    private final HazelcastInstance hazelcast;
    
    private final ConcurrentMap<String, IQueue<ProcessorMessage>> queuesCache = new ConcurrentHashMap<>();
    
    private final ProcessorRouteTable routeTable;
    
    public ProcessorDispatcher(HazelcastInstance hazelcast, ProcessorRouteTable routeTable)
    {
        super();
        this.hazelcast = Objects.requireNonNull(hazelcast);
        this.routeTable = Objects.requireNonNull(routeTable);
        // Setup a object listener to clean up our queue cache
        this.hazelcast.addDistributedObjectListener(new QueueListener());
    }
    
    /**
     * Place the given pool message onto the queue for a random processing pool
     * @param message the message
     * @return whether the message was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatch(ProcessorMessage message)
    {
        UUID processor = route(message);
        if (processor == null) return PublishStatus.Unroutable;
        // Offer onto the result queue
        IQueue<ProcessorMessage> queue = this.getProcessorQueue(Objects.requireNonNull(processor));
        boolean success = queue.offer(message);
        return success ? PublishStatus.Success : PublishStatus.Failed;
    }
    
    /**
     * Place the given processor message onto the queue for the given processor
     * @param processor the id of the processor
     * @param message the message
     * @return whether the message was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatch(UUID processor, ProcessorMessage message)
    {
        if (processor == null)
            processor = route(message);
        // Offer onto the result queue
        IQueue<ProcessorMessage> queue = this.getProcessorQueue(Objects.requireNonNull(processor));
        boolean success = queue.offer(message);
        return success ? PublishStatus.Success : PublishStatus.Failed;
    }
    
    private UUID route(ProcessorMessage message)
    {
        return (message instanceof ProcessorHashable) ? this.routeTable.routeProcessor(((ProcessorHashable) message).routeHash()) : this.routeTable.routeProcessor();
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
     * Place the given result onto the result queue for the given processor
     * @param processor the id of the processor
     * @param result the result
     * @return whether the result was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatchResult(UUID processor, ResultMessage result)
    {
        return this.dispatch(processor, result);
    }
    
    /**
     * Place the given reading onto the reading queue for a random processor
     * @param reading the reading
     * @return whether the reading was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatchReading(ReadingParcelMO reading)
    {
        return this.dispatch(reading);
    }
    
    /**
     * Place the given reading onto the reading queue for the given processing pool
     * @param processor the id of the processor
     * @param reading the reading
     * @return whether the reading was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    public PublishStatus dispatchReading(UUID processor, ReadingParcelMO reading)
    {
        return this.dispatch(processor, reading);
    }
    
    /**
     * Place the given Agent action onto the queue for the processing pool
     * @param action the Agent action
     * @return whether the Agent action was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed}) 
     */
    public PublishStatus dispatchAgentMessage(AgentMessage action)
    {
        return this.dispatch(action);
    }
    
    private IQueue<ProcessorMessage> getProcessorQueue(UUID processorId)
    {
        return this.queuesCache.computeIfAbsent(HZNames.buildProcessorQueueName(processorId), this.hazelcast::getQueue);
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
                ProcessorDispatcher.this.queuesCache.remove(event.getObjectName());
            }
        }
    }
}
