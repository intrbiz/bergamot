package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.who")
public class CheckWho extends Message
{
    private static final long serialVersionUID = 1L;
    
    public CheckWho()
    {
        super();
    }

    public CheckWho(Message message)
    {
        super(message);
    }
}
