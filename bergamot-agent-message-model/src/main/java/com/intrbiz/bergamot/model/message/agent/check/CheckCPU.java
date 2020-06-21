package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.cpu")
public class CheckCPU extends Message
{
    private static final long serialVersionUID = 1L;
    
    public CheckCPU()
    {
        super();
    }

    public CheckCPU(Message message)
    {
        super(message);
    }
}
