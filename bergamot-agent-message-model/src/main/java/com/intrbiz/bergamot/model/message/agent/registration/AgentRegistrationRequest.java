package com.intrbiz.bergamot.model.message.agent.registration;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Request this agent to be registered using the template as identified by the 
 * client certificate and with the host name provided and public key provided.
 *
 */
@JsonTypeName("bergamot.agent.registration.request")
public class AgentRegistrationRequest extends AgentRegistrationMessage
{
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
    
    public AgentRegistrationRequest()
    {
        super();
    }

    public AgentRegistrationRequest(String id)
    {
        super(id);
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

    public UUID getAgentId()
    {
        return agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }
}
