package com.intrbiz.bergamot.worker.engine;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;

/**
 * Executors execute a particular check.
 * 
 * Note: executors must be thread safe!
 */
public interface Executor<T extends Engine>
{

    void prepare(T engine, EngineContext context) throws Exception;
    
    void start(T engine, EngineContext context) throws Exception;
    
    /**
     * Should this executor be used to execute the given check
     * @return true if this executor can execute the given check
     */
    boolean accept(ExecuteCheck check);
    
    /**
     * Execute the check
     * 
     * Note: An executor must only throw an exception in the event of a transient error, 
     * where the check is to be retried.  Ideally exceptions should be handled by the 
     * executor by submitting an ERROR result. 
     * 
     */
    void execute(ExecuteCheck check, CheckExecutionContext context);

}
