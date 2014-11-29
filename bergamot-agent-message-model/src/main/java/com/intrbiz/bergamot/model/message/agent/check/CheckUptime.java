package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.uptime")
public class CheckUptime extends AgentMessage
{
    public CheckUptime()
    {
        super();
    }

    public CheckUptime(AgentMessage message)
    {
        super(message);
    }

    public CheckUptime(String id)
    {
        super(id);
    }
}
