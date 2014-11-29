package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.mem")
public class CheckMem extends AgentMessage
{
    public CheckMem()
    {
        super();
    }

    public CheckMem(AgentMessage message)
    {
        super(message);
    }

    public CheckMem(String id)
    {
        super(id);
    }
}
