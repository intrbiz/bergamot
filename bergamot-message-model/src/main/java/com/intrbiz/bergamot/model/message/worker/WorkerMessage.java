package com.intrbiz.bergamot.model.message.worker;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;

public class WorkerMessage extends Message
{
    private static final long serialVersionUID = 1L;

    /**
     * The worker to which this message is destined
     */
    @JsonProperty("worker_id")
    private UUID workerId;

    public WorkerMessage()
    {
        super();
    }

    public WorkerMessage(Message replyTo)
    {
        super(replyTo);
    }

    public WorkerMessage(UUID replyTo)
    {
        super(replyTo);
    }

    public UUID getWorkerId()
    {
        return this.workerId;
    }

    public void setWorkerId(UUID workerId)
    {
        this.workerId = workerId;
    }
}
