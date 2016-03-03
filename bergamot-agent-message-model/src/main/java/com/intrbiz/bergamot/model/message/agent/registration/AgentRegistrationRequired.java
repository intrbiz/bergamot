package com.intrbiz.bergamot.model.message.agent.registration;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;

@JsonTypeName("bergamot.agent.registration.required")
public class AgentRegistrationRequired extends AgentRegistrationMessage
{
    public AgentRegistrationRequired()
    {
        super();
    }

    public AgentRegistrationRequired(AgentMessage inResponseTo)
    {
        super(inResponseTo);
    }

    public AgentRegistrationRequired(String id)
    {
        super(id);
    }
}
