package com.intrbiz.bergamot.model.message.proxy;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Lookup agent key
 */
@JsonTypeName("bergamot.proxy.agent.state")
public class AgentState extends ProxyMessage
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("agent_id")
    private UUID agentId;
    
    @JsonProperty("connected")
    private boolean connected;
    
    public AgentState()
    {
        super();
    }

    public AgentState(UUID agentId, boolean connected)
    {
        super();
        this.agentId = agentId;
        this.connected = connected;
    }

    public UUID getAgentId()
    {
        return this.agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }

    public boolean isConnected()
    {
        return this.connected;
    }

    public void setConnected(boolean connected)
    {
        this.connected = connected;
    }
}
