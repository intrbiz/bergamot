package com.intrbiz.bergamot.command;

import com.intrbiz.bergamot.command.handler.RegisterBergamotAgentHandler;

public class DefaultCommandProcessor extends AbstractCommandProcessor
{
    public DefaultCommandProcessor()
    {
        super();
        // register some commands
        this.registerHandler(new RegisterBergamotAgentHandler());
    }
}
