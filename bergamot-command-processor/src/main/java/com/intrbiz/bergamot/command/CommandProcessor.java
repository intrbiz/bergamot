package com.intrbiz.bergamot.command;

import com.intrbiz.bergamot.command.handler.BergamotCommandHandler;
import com.intrbiz.bergamot.model.message.command.CommandRequest;

public interface CommandProcessor
{
    int getThreads();
    
    void setThreads(int threads);
    
    void start();
    
    void registerHandler(BergamotCommandHandler<?> handler);
    
    BergamotCommandHandler<?> getHandler(Class<? extends CommandRequest> forRequestType);
}
