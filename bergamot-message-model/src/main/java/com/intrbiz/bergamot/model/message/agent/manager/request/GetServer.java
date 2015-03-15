package com.intrbiz.bergamot.model.message.agent.manager.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.get_agent")
public class GetServer extends AgentManagerRequest
{
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("common_name")
    private String commonName;
    
    public GetServer()
    {
        super();
    }
    
    public GetServer(UUID siteId, String commonName)
    {
        super();
        this.siteId = siteId;
        this.commonName = commonName;
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
}
