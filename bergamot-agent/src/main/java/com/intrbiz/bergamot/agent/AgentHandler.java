package com.intrbiz.bergamot.agent;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;

public interface AgentHandler
{
    Class<?>[] getMessages();
    
    AgentMessage handle(AgentMessage request);
    
    void init(BergamotAgent agent);
}
