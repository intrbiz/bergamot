package com.intrbiz.bergamot.model.message.agent.manager.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.sign_server")
public class SignServer extends AgentManagerRequest
{
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("common_name")
    private String commonName;
    
    @JsonProperty("public_key_pem")
    private String publicKeyPEM;
    
    public SignServer()
    {
        super();
    }
    
    public SignServer(UUID siteId, String commonName, String publicKeyPEM)
    {
        super();
        this.siteId = siteId;
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
