package com.intrbiz.bergamot.model.message.agent.ping;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.pong")
public class AgentPong extends AgentMessage
{
    public AgentPong()
    {
        super();
    }

    public AgentPong(AgentMessage message)
    {
        super(message);
    }

    public AgentPong(String id)
    {
        super(id);
    }
}
