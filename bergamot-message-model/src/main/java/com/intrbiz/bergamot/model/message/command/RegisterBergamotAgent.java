package com.intrbiz.bergamot.model.message.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.command.register-agent")
public class RegisterBergamotAgent extends CommandRequest
{
    @JsonProperty("template_id")
    private UUID templateId;
    
    /**
     * The UUID of this agent;
     */
    @JsonProperty("agent_id")
    private UUID agentId;
    
    /**
     * The agent common name (Host Name)
     */
    @JsonProperty("common_name")
    private String commonName;
    
    /**
     * The RSA public key in PEM
     */
    @JsonProperty("public_key")
    private String publicKey;
    
    public RegisterBergamotAgent()
    {
        super();
    }

    public UUID getAgentId()
    {
        return agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }

    public String getCommonName()
    {
        return commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public UUID getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(UUID templateId)
    {
        this.templateId = templateId;
    }
}
