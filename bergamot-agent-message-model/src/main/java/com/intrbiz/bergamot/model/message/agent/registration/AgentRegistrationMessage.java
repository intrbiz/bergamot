package com.intrbiz.bergamot.model.message.agent.registration;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;

/**
 * Base class for all registration messages
 *
 */
public abstract class AgentRegistrationMessage extends AgentMessage
{
    public AgentRegistrationMessage()
    {
        super();
    }

    public AgentRegistrationMessage(AgentMessage message)
    {
        super(message);
    }

    public AgentRegistrationMessage(String id)
    {
        super(id);
    }
}
