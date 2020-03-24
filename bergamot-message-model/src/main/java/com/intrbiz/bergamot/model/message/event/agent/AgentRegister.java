package com.intrbiz.bergamot.model.message.event.agent;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An agent has connected
 */
@JsonTypeName("bergamot.event.agent.register")
public class AgentRegister extends AgentEvent
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
    
    @JsonProperty("agent_summary")
    private String agentSummary;
    
    @JsonProperty("agent_address")
    private String agentAddress;
    
    @JsonProperty("template_name")
    private String templateName;
    
    public AgentRegister()
    {
        super();
    }

    public AgentRegister(UUID siteId, UUID agentId, UUID keyId, String agentName, String agentSummary, String agentAddress, String templateName)
    {
        super();
        this.siteId = Objects.requireNonNull(siteId);
        this.agentId = Objects.requireNonNull(agentId);
        this.keyId = Objects.requireNonNull(keyId);
        this.agentName = Objects.requireNonNull(agentName);
        this.agentSummary = agentSummary;
        this.agentAddress = agentAddress;
        this.templateName = Objects.requireNonNull(templateName);
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

    public String getAgentSummary()
    {
        return this.agentSummary;
    }

    public void setAgentSummary(String agentSummary)
    {
        this.agentSummary = agentSummary;
    }

    public String getAgentAddress()
    {
        return this.agentAddress;
    }

    public void setAgentAddress(String agentAddress)
    {
        this.agentAddress = agentAddress;
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
