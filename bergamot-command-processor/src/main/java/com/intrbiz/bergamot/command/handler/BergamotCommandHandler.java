package com.intrbiz.bergamot.command.handler;

import com.intrbiz.bergamot.command.CommandProcessor;
import com.intrbiz.bergamot.model.message.command.CommandRequest;
import com.intrbiz.bergamot.model.message.command.CommandResponse;

public interface BergamotCommandHandler<T extends CommandRequest>
{
    void init(CommandProcessor processor);
    
    CommandResponse process(T request);
    
    Class<? extends T>[] handles();
}
