package com.intrbiz.bergamot.model.message.agent.ping;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.ping")
public class AgentPing extends AgentMessage
{
    public AgentPing()
    {
        super();
    }

    public AgentPing(AgentMessage message)
    {
        super(message);
    }

    public AgentPing(String id)
    {
        super(id);
    }
}
