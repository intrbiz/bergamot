package com.intrbiz.bergamot.model.message.agent.manager.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.sign_template")
public class SignTemplate extends AgentManagerRequest
{
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("template_name")
    private String templateName;
    
    @JsonProperty("public_key_pem")
    private String publicKeyPEM;
    
    public SignTemplate()
    {
        super();
    }
    
    public SignTemplate(UUID siteId, UUID id, String templateName, String publicKeyPEM)
    {
        super();
        this.siteId = siteId;
        this.id = id;
        this.templateName = templateName;
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

    public String getTemplateName()
    {
        return templateName;
    }

    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
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
