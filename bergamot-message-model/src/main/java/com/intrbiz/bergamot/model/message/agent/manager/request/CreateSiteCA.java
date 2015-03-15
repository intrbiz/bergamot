package com.intrbiz.bergamot.model.message.agent.manager.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.create-site-ca")
public class CreateSiteCA extends AgentManagerRequest
{
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("site_name")
    private String siteName;
    
    public CreateSiteCA()
    {
        super();
    }
    
    public CreateSiteCA(UUID siteId, String siteName)
    {
        super();
        this.siteId = siteId;
        this.siteName = siteName;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }
}
