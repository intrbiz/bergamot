package com.intrbiz.bergamot.model.message.processor.agent;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.processor.ProcessorHashable;
import com.intrbiz.bergamot.model.message.processor.ProcessorMessage;

public abstract class AgentMessage extends ProcessorMessage implements ProcessorHashable
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("agent_id")
    private UUID agentId;
    
    public AgentMessage()
    {
        super();
    }
    
    public AgentMessage(UUID siteId, UUID agentId)
    {
        super();
        this.siteId = Objects.requireNonNull(siteId);
        this.agentId = Objects.requireNonNull(agentId);
    }
    
    public UUID getSiteId()
    {
        return this.siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public UUID getAgentId()
    {
        return this.agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }
    
    @Override
    public long routeHash()
    {
        return this.agentId.getLeastSignificantBits();
    }
}
