package com.intrbiz.bergamot.command.handler;

import com.intrbiz.bergamot.model.message.command.CommandRequest;
import com.intrbiz.bergamot.model.message.command.CommandResponse;

public interface BergamotCommandHandler
{
    CommandResponse process(CommandRequest request);
    
    Class<? extends CommandRequest>[] handles();
}
