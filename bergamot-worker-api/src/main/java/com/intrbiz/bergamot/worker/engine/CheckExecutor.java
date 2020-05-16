package com.intrbiz.bergamot.worker.engine;

import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;

/**
 * Executors execute a particular check.
 * 
 * Note: executors must be thread safe!
 */
public interface CheckExecutor<T extends CheckEngine>
{
    String getName();

    void prepare(T engine, CheckEngineContext context) throws Exception;
    
    void start(T engine, CheckEngineContext context) throws Exception;
    
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
