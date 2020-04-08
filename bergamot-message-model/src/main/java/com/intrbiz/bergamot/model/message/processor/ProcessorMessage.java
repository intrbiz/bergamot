package com.intrbiz.bergamot.model.message.processor;

import com.intrbiz.bergamot.model.message.Message;


/**
 * A message which is directed to a processing pool
 */
public abstract class ProcessorMessage extends Message
{
    private static final long serialVersionUID = 1L;

    public ProcessorMessage()
    {
        super();
    }
}
