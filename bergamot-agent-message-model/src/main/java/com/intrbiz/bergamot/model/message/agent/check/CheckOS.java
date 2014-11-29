package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.os")
public class CheckOS extends AgentMessage
{
    public CheckOS()
    {
        super();
    }

    public CheckOS(AgentMessage message)
    {
        super(message);
    }

    public CheckOS(String id)
    {
        super(id);
    }
}
