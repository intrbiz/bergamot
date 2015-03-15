package com.intrbiz.bergamot.model.message.agent.manager.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.sign_agent")
public class SignAgent extends AgentManagerRequest
{
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("common_name")
    private String commonName;
    
    @JsonProperty("public_key_pem")
    private String publicKeyPEM;
    
    public SignAgent()
    {
        super();
    }
    
    public SignAgent(UUID siteId, UUID id, String commonName, String publicKeyPEM)
    {
        super();
        this.siteId = siteId;
        this.id = id;
        this.commonName = commonName;
        this.publicKeyPEM = publicKeyPEM;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getCommonName()
    {
        return commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }

    public String getPublicKeyPEM()
    {
        return publicKeyPEM;
    }

    public void setPublicKeyPEM(String publicKeyPEM)
    {
        this.publicKeyPEM = publicKeyPEM;
    }
}
