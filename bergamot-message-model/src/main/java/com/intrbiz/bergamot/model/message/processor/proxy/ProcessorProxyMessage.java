package com.intrbiz.bergamot.model.message.processor.proxy;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;

public abstract class ProcessorProxyMessage extends ProcessorMessage
{
    private static final long serialVersionUID = 1L;
    
    public ProcessorProxyMessage()
    {
        super();
    }

    public ProcessorProxyMessage(Message replyTo)
    {
        super(replyTo);
    }

    public ProcessorProxyMessage(UUID replyTo)
    {
        super(replyTo);
    }
}
