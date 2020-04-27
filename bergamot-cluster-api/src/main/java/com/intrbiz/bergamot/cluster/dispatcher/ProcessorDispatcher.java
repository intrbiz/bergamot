package com.intrbiz.bergamot.cluster.dispatcher;

import java.util.UUID;

import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;

/**
 * Dispatch results, readings, agent message to a processor
 */
public interface ProcessorDispatcher
{   
    /**
     * Place the given processor message onto the queue for the given processor
     * @param processor the id of the processor
     * @param message the message
     * @return whether the message was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    PublishStatus dispatch(UUID processor, ProcessorMessage message);
    
    /**
     * Place the given pool message onto the queue for a random processing pool
     * @param message the message
     * @return whether the message was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    PublishStatus dispatch(ProcessorMessage message);

    /**
     * Place the given result onto the result queue for a random processing pool
     * @param result the result
     * @return whether the result was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    PublishStatus dispatchResult(ResultMessage result);
    
    /**
     * Place the given result onto the result queue for the given processor
     * @param processor the id of the processor
     * @param result the result
     * @return whether the result was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    PublishStatus dispatchResult(UUID processor, ResultMessage result);
    
    /**
     * Place the given reading onto the reading queue for a random processor
     * @param reading the reading
     * @return whether the reading was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    PublishStatus dispatchReading(ReadingParcelMO reading);
    
    /**
     * Place the given reading onto the reading queue for the given processing pool
     * @param processor the id of the processor
     * @param reading the reading
     * @return whether the reading was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    PublishStatus dispatchReading(UUID processor, ReadingParcelMO reading);
    
    /**
     * Place the given Agent action onto the queue for the processing pool
     * @param action the Agent action
     * @return whether the Agent action was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed}) 
     */
    PublishStatus dispatchAgentMessage(AgentMessage action);
}
