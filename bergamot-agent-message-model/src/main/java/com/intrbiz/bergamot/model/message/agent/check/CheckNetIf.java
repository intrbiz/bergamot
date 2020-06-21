package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.netif")
public class CheckNetIf extends Message
{
    private static final long serialVersionUID = 1L;
    
    public CheckNetIf()
    {
        super();
    }

    public CheckNetIf(Message message)
    {
        super(message);
    }
}
