package com.intrbiz.bergamot.command.handler;

import com.intrbiz.bergamot.model.message.command.CommandRequest;
import com.intrbiz.bergamot.model.message.command.CommandResponse;

public interface BergamotCommandHandler<T extends CommandRequest>
{
    CommandResponse process(T request);
    
    Class<? extends T>[] handles();
}
