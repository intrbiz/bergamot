package com.intrbiz.bergamot.agent;

import com.intrbiz.bergamot.model.message.Message;

public interface AgentHandler
{
    Class<?>[] getMessages();
    
    Message handle(Message request);
    
    void init(BergamotAgent agent);
}
