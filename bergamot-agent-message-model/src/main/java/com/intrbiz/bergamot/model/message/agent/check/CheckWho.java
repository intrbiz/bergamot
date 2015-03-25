package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.who")
public class CheckWho extends AgentMessage
{
    public CheckWho()
    {
        super();
    }

    public CheckWho(AgentMessage message)
    {
        super(message);
    }

    public CheckWho(String id)
    {
        super(id);
    }
}
