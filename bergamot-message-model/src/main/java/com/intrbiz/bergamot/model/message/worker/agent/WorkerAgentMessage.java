package com.intrbiz.bergamot.model.message.worker.agent;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.worker.WorkerMessage;

public abstract class WorkerAgentMessage extends WorkerMessage
{
    private static final long serialVersionUID = 1L;
    
    public WorkerAgentMessage()
    {
        super();
    }

    public WorkerAgentMessage(Message replyTo)
    {
        super(replyTo);
    }

    public WorkerAgentMessage(UUID replyTo)
    {
        super(replyTo);
    }
}
