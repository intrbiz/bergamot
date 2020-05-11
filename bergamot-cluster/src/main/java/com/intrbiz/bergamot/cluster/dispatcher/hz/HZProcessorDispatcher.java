package com.intrbiz.bergamot.cluster.dispatcher.hz;

import java.util.Objects;
import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.cluster.registry.ProcessorRouteTable;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.model.message.processor.ProcessorHashable;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.ProcessorAgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;

/**
 * Dispatch results, readings, agent message to a processor
 */
public class HZProcessorDispatcher extends HZBaseDispatcher<ProcessorMessage> implements ProcessorDispatcher
{
    private final ProcessorRouteTable routeTable;
    
    public HZProcessorDispatcher(HazelcastInstance hazelcast, ProcessorRouteTable routeTable)
    {
        super(hazelcast, HZNames::buildProcessorRingbufferName);
        this.routeTable = Objects.requireNonNull(routeTable);
    }
    
    /**
     * Place the given pool message onto the queue for a random processing pool
     * @param message the message
     * @return whether the message was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    @Override
    public PublishStatus dispatch(ProcessorMessage message)
    {
        // Pick a processor at random if we have no processor id
        // TODO: it would be nice to validate the processor exists
        if (message.getProcessorId() == null)
        {
            message.setProcessorId(route(message));
        }
        // Did we manage to route the message
        if (message.getProcessorId() == null)
        {
            return PublishStatus.Unroutable;
        }
        // Offer onto the result queue
        return this.offer(message.getProcessorId(), message);
    }
    
    private UUID route(ProcessorMessage message)
    {
        return (message instanceof ProcessorHashable) ? 
                this.routeTable.routeProcessor(((ProcessorHashable) message).routeHash()) : 
                this.routeTable.routeProcessor();
    }

    /**
     * Place the given result onto the result queue for a random processing pool
     * @param result the result
     * @return whether the result was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    @Override
    public PublishStatus dispatchResult(ResultMessage result)
    {
        return this.dispatch(result);
    }
    
    /**
     * Place the given reading onto the reading queue for a random processor
     * @param reading the reading
     * @return whether the reading was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    @Override
    public PublishStatus dispatchReading(ReadingParcelMessage reading)
    {
        return this.dispatch(reading);
    }
    
    /**
     * Place the given Agent action onto the queue for the processing pool
     * @param action the Agent action
     * @return whether the Agent action was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed}) 
     */
    @Override
    public PublishStatus dispatchAgentMessage(ProcessorAgentMessage action)
    {
        return this.dispatch(action);
    }
}
