package com.intrbiz.bergamot.model.message.agent.manager.request;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.manager.AgentManagerRequest;

@JsonTypeName("bergamot.agent.manager.get_root_ca")
public class GetRootCA extends AgentManagerRequest
{   
    public GetRootCA()
    {
        super();
    }
}
