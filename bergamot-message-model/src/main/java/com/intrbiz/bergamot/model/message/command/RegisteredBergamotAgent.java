package com.intrbiz.bergamot.model.message.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.command.registered-agent")
public class RegisteredBergamotAgent extends CommandResponse
{
    @JsonProperty("agent_id")
    private UUID agentId;
    
    @JsonProperty("common_name")
    private String commonName;
    
    @JsonProperty("certificate")
    private String certificate;

    public RegisteredBergamotAgent()
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

    public String getCertificate()
    {
        return certificate;
    }

    public void setCertificate(String certificate)
    {
        this.certificate = certificate;
    }
}
