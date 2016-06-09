package com.intrbiz.bergamot.model.message.agent.registration;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

/**
 * The agent registration request has been completed successfully
 *
 */
@JsonTypeName("bergamot.agent.registration.complete")
public class AgentRegistrationComplete extends AgentRegistrationMessage
{
    @JsonProperty("agent_id")
    private UUID agentId;
    
    @JsonProperty("common_name")
    private String commonName;
    
    @JsonProperty("certificate")
    private String certificate;
    
    public AgentRegistrationComplete()
    {
        super();
    }

    public AgentRegistrationComplete(AgentMessage inResponseTo)
    {
        super(inResponseTo);
    }
    
    public AgentRegistrationComplete(AgentMessage inResponseTo, UUID agentId, String commonName, String certificate)
    {
        super(inResponseTo);
        this.agentId = agentId;
        this.commonName = commonName;
        this.certificate = certificate;
    }

    public String getCertificate()
    {
        return certificate;
    }

    public void setCertificate(String certificate)
    {
        this.certificate = certificate;
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
}
