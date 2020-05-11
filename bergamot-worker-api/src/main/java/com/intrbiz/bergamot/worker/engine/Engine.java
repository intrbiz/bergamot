package com.intrbiz.bergamot.worker.engine;

import java.util.Collection;

import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public interface Engine
{    
    String getName();
    
    Collection<Executor<?>> getExecutors();
    
    void prepare(EngineContext context) throws Exception;
    
    void start(EngineContext context) throws Exception;
    
    boolean accept(ExecuteCheck task);
    
    void execute(ExecuteCheck task, CheckExecutionContext context);
    
    void shutdown(EngineContext engineContext);
}
