package com.intrbiz.bergamot.model.message.agent.manager.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.get-site-ca")
public class GetSiteCA extends AgentManagerRequest
{
    @JsonProperty("site_id")
    private UUID siteId;
    
    public GetSiteCA()
    {
        super();
    }
    
    public GetSiteCA(UUID siteId)
    {
        super();
        this.siteId = siteId;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }
}
