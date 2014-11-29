package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.netif")
public class CheckNetIf extends AgentMessage
{
    public CheckNetIf()
    {
        super();
    }

    public CheckNetIf(AgentMessage message)
    {
        super(message);
    }

    public CheckNetIf(String id)
    {
        super(id);
    }
}
