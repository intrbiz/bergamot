package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.mem")
public class CheckMem extends Message
{
    private static final long serialVersionUID = 1L;
    
    public CheckMem()
    {
        super();
    }

    public CheckMem(Message message)
    {
        super(message);
    }
}
