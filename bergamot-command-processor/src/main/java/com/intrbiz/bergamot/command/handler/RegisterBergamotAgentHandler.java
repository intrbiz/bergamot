package com.intrbiz.bergamot.command.handler;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.message.command.CommandResponse;
import com.intrbiz.bergamot.model.message.command.GeneralCommandError;
import com.intrbiz.bergamot.model.message.command.RegisterBergamotAgent;

public class RegisterBergamotAgentHandler implements BergamotCommandHandler<RegisterBergamotAgent>
{
    private Logger logger = Logger.getLogger(RegisterBergamotAgentHandler.class);
    
    @Override
    public CommandResponse process(RegisterBergamotAgent request)
    {
        logger.info("Registering agent: " + request.getCommonName() + " (" + request.getAgentId() + ")");
        //
        return new GeneralCommandError("Not yet implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends RegisterBergamotAgent>[] handles()
    {
        return new Class[] { RegisterBergamotAgent.class };
    }
}
