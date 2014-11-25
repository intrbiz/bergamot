package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.cpu")
public class CheckCPU extends AgentMessage
{
    public CheckCPU()
    {
        super();
    }

    public CheckCPU(AgentMessage message)
    {
        super(message);
    }

    public CheckCPU(String id)
    {
        super(id);
    }
}
