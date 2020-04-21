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
    @JsonProperty("processor")
    private UUID processor;
    
    /**
     * The worker which generated this message
     */
    @JsonProperty("worker")
    private UUID worker;

    public ProcessorMessage()
    {
        super();
    }

    public UUID getProcessor()
    {
        return this.processor;
    }

    public void setProcessor(UUID processor)
    {
        this.processor = processor;
    }

    public UUID getWorker()
    {
        return this.worker;
    }

    public void setWorker(UUID worker)
    {
        this.worker = worker;
    }
}
