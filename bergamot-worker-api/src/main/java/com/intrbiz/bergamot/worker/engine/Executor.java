package com.intrbiz.bergamot.worker.engine;

import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.result.Result;
import com.intrbiz.configuration.Configurable;

/**
 * Executors execute a particular task.
 * 
 * Note: executors must be thread safe!
 */
public interface Executor<T extends Engine> extends Configurable<ExecutorCfg>
{
    T getEngine();
    
    void setEngine(T engine);
    
    boolean accept(ExecuteCheck task);
    
    /**
     * Execute the check
     */
    Result execute(ExecuteCheck executeCheck);
    
    void start() throws Exception;
}
