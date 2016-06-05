package com.intrbiz.bergamot.agent.handler;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.agent.AgentHandler;
import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationComplete;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationFailed;
import com.intrbiz.bergamot.model.message.agent.registration.AgentRegistrationRequired;

public class AgentRegistrationHandler implements AgentHandler
{
    private Logger logger = Logger.getLogger(AgentRegistrationHandler.class);

    public AgentRegistrationHandler()
    {
        super();
    }

    @Override
    public Class<?>[] getMessages()
    {
        return new Class[] { AgentRegistrationRequired.class, AgentRegistrationComplete.class, AgentRegistrationFailed.class };
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        if (request instanceof AgentRegistrationRequired)
        {
            logger.info("Starting agent registration process");
        }
        else if (request instanceof AgentRegistrationComplete)
        {
            logger.info("Successfully got signed agent certificate");
        }
        else if (request instanceof AgentRegistrationFailed)
        {
            logger.error("Failed to get signed agent certificate: " + ((AgentRegistrationFailed) request).getErrorCode() + " " + ((AgentRegistrationFailed) request).getMessage());
        }
        else
        {
            logger.warn("Ignoring unexpected registration message: " + request);
        }
        return null;
    }
}
