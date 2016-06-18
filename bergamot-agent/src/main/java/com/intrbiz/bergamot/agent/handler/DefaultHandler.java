package com.intrbiz.bergamot.agent.handler;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.agent.AgentMessage;
import com.intrbiz.bergamot.model.message.agent.error.GeneralError;

public class DefaultHandler extends AbstractAgentHandler
{
    private Logger logger = Logger.getLogger(DefaultHandler.class);

    public DefaultHandler()
    {
        super();
    }

    @Override
    public Class<?>[] getMessages()
    {
        return new Class[0];
    }

    @Override
    public AgentMessage handle(AgentMessage request)
    {
        // unhandled
        logger.warn("Unhandled message: " + request);
        return new GeneralError(request, "Unimplemented");
    }
}
