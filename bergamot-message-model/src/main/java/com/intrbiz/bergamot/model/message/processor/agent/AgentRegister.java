package com.intrbiz.bergamot.model.message.processor.agent;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.processor.ProcessorHashable;

/**
 * An agent has connected
 */
@JsonTypeName("bergamot.processor.agent.register")
public class AgentRegister extends ProcessorAgentMessage implements ProcessorHashable
{
    private static final long serialVersionUID = 1L;
    
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
        this.setSiteId(Objects.requireNonNull(siteId));
        this.agentId = Objects.requireNonNull(agentId);
        this.keyId = Objects.requireNonNull(keyId);
        this.agentName = Objects.requireNonNull(agentName);
        this.agentSummary = agentSummary;
        this.agentAddress = agentAddress;
        this.templateName = Objects.requireNonNull(templateName);
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
    
    @Override
    public long routeHash()
    {
        return this.agentId.getLeastSignificantBits();
    }
}
