package com.intrbiz.bergamot.agent.handler;

import com.intrbiz.bergamot.agent.AgentHandler;
import com.intrbiz.bergamot.agent.BergamotAgent;

public abstract class AbstractAgentHandler implements AgentHandler
{
    protected BergamotAgent agent;
    
    @Override
    public void init(BergamotAgent agent)
    {
        this.agent = agent;
    }
    
    public BergamotAgent getAgent()
    {
        return this.agent;
    }
}
