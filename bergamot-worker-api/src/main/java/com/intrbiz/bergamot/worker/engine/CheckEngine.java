package com.intrbiz.bergamot.worker.engine;

import java.util.Collection;

import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

public interface CheckEngine
{   
    String getName();
    
    String getVendor();
    
    boolean isEnabledByDefault();
    
    Collection<CheckExecutor<?>> getExecutors();
    
    void prepare(CheckEngineContext context) throws Exception;
    
    void start(CheckEngineContext context) throws Exception;
    
    boolean accept(ExecuteCheck task);
    
    void execute(ExecuteCheck task, CheckExecutionContext context);
    
    void shutdown(CheckEngineContext engineContext);
}
