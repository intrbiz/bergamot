package com.intrbiz.bergamot.cluster.dispatcher;

import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;
import com.intrbiz.bergamot.model.message.processor.agent.ProcessorAgentMessage;
import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;

/**
 * Dispatch results, readings, agent message to a processor
 */
public interface ProcessorDispatcher
{    
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
     * Place the given reading onto the reading queue for a random processor
     * @param reading the reading
     * @return whether the reading was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed})
     */
    PublishStatus dispatchReading(ReadingParcelMessage reading);
    
    /**
     * Place the given Agent action onto the queue for the processing pool
     * @param action the Agent action
     * @return whether the Agent action was offered successfully ({@code PublishStatus.Success}) or not ({@code PublishStatus.Failed}) 
     */
    PublishStatus dispatchAgentMessage(ProcessorAgentMessage action);
}
