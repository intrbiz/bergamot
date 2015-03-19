package com.intrbiz.bergamot.model.message.agent.manager.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.get_agent")
public class GetServer extends AgentManagerRequest
{    
    @JsonProperty("common_name")
    private String commonName;
    
    public GetServer()
    {
        super();
    }
    
    public GetServer(String commonName)
    {
        super();
        this.commonName = commonName;
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
