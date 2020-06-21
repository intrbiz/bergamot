package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.agent")
public class CheckAgent extends Message
{
    private static final long serialVersionUID = 1L;

    public CheckAgent()
    {
        super();
    }

    public CheckAgent(Message message)
    {
        super(message);
    }
}
