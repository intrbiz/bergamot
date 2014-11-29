package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.check.disk")
public class CheckDisk extends AgentMessage
{    
    public CheckDisk()
    {
        super();
    }

    public CheckDisk(AgentMessage message)
    {
        super(message);
    }

    public CheckDisk(String id)
    {
        super(id);
    }
}
