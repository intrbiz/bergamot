package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.agent")
public class CheckAgent extends AgentMessage
{
    public CheckAgent()
    {
        super();
    }

    public CheckAgent(AgentMessage message)
    {
        super(message);
    }

    public CheckAgent(String id)
    {
        super(id);
    }
}
