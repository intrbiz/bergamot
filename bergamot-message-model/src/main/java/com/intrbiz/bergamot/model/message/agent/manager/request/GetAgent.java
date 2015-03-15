package com.intrbiz.bergamot.model.message.agent.manager.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.get_agent")
public class GetAgent extends AgentManagerRequest
{
    @JsonProperty("site_id")
    private UUID siteId;
    
    @JsonProperty("id")
    private UUID id;
    
    public GetAgent()
    {
        super();
    }
    
    public GetAgent(UUID siteId, UUID id)
    {
        super();
        this.siteId = siteId;
        this.id = id;
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
}
