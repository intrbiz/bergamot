package com.intrbiz.bergamot.model.message.event.agent;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An agent has connected
 */
@JsonTypeName("bergamot.event.agent.connect")
public class AgentConnect extends AgentEvent
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("agent_id")
    private UUID agentId;
    
    @JsonProperty("key_id")
    private UUID keyId;
    
    @JsonProperty("agent_name")
    private String agentName;
    
    @JsonProperty("template_name")
    private String templateName;
    
    public AgentConnect()
    {
        super();
    }

    public AgentConnect(UUID siteId, UUID agentId, UUID keyId, String agentName, String templateName)
    {
        super();
        this.siteId = siteId;
        this.agentId = agentId;
        this.keyId = keyId;
        this.agentName = agentName;
        this.templateName = templateName;
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

    public UUID getKeyId()
    {
        return this.keyId;
    }

    public void setKeyId(UUID keyId)
    {
        this.keyId = keyId;
    }

    public String getAgentName()
    {
        return this.agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public String getTemplateName()
    {
        return this.templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }
}
