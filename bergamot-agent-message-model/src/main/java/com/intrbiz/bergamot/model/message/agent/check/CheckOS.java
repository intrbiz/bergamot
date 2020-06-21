package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.os")
public class CheckOS extends Message
{
    private static final long serialVersionUID = 1L;
    
    public CheckOS()
    {
        super();
    }

    public CheckOS(Message message)
    {
        super(message);
    }
}
