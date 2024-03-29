package com.intrbiz.bergamot.model.message.agent.check;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.Message;

@JsonTypeName("bergamot.agent.check.disk")
public class CheckDisk extends Message
{   
    private static final long serialVersionUID = 1L;
    
    public CheckDisk()
    {
        super();
    }

    public CheckDisk(Message message)
    {
        super(message);
    }
}
