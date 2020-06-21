package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.uptime")
public class CheckUptime extends Message
{
    private static final long serialVersionUID = 1L;
    
    public CheckUptime()
    {
        super();
    }

    public CheckUptime(Message message)
    {
        super(message);
    }
}
