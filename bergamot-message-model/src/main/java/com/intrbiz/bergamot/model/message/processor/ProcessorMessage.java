package com.intrbiz.bergamot.model.message.processor;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;

/**
 * A message which is directed to a processing pool
 */
public abstract class ProcessorMessage extends Message
{
    private static final long serialVersionUID = 1L;

    /**
     * The processor to which this message is destined
     */
    @JsonProperty("processor_id")
    private UUID processorId;

    public ProcessorMessage()
    {
        super();
    }

    public ProcessorMessage(Message replyTo)
    {
        super(replyTo);
    }

    public ProcessorMessage(UUID replyTo)
    {
        super(replyTo);
    }

    public UUID getProcessorId()
    {
        return this.processorId;
    }

    public void setProcessorId(UUID processorId)
    {
        this.processorId = processorId;
    }
}
