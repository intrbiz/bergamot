package com.intrbiz.bergamot.model.message.processor.agent;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;

public abstract class ProcessorAgentMessage extends ProcessorMessage
{
    private static final long serialVersionUID = 1L;
    
    public ProcessorAgentMessage()
    {
        super();
    }

    public ProcessorAgentMessage(Message replyTo)
    {
        super(replyTo);
    }

    public ProcessorAgentMessage(UUID replyTo)
    {
        super(replyTo);
    }
}
