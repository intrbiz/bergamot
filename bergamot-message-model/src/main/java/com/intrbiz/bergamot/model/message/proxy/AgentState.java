package com.intrbiz.bergamot.model.message.proxy;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * State update for an agent via the proxy protocol
 */
@JsonTypeName("bergamot.proxy.agent.state")
public class AgentState extends ProxyMessage
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("agent_id")
    private UUID agentId;
    
    @JsonProperty("nonce")
    private UUID nonce;
    
    @JsonProperty("connected")
    private boolean connected;
    
    public AgentState()
    {
        super();
    }

    public AgentState(UUID agentId, UUID nonce, boolean connected)
    {
        super();
        this.agentId = agentId;
        this.nonce = nonce;
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

    public UUID getNonce()
    {
        return this.nonce;
    }

    public void setNonce(UUID nonce)
    {
        this.nonce = nonce;
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
