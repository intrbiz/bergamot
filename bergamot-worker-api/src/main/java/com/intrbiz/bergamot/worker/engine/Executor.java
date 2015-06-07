package com.intrbiz.bergamot.worker.engine;

import java.util.function.Consumer;

import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
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
    
    /**
     * Should this executor be used to execute the given check
     * @return true if this executor can execute the given check
     */
    boolean accept(ExecuteCheck task);
    
    /**
     * Execute the check
     * 
     * Note: An executor must only throw an exception in the event of a transient error, 
     * where the check is to be retried.  Ideally exceptions should be handled by the 
     * executor by submitting an ERROR result. 
     * 
     */
    void execute(ExecuteCheck executeCheck, Consumer<ResultMO> resultSubmitter);
    
    /**
     * Publish a result out of band
     */
    void publishResult(ResultKey key, ResultMO resultMO);
    
    /**
     * Publish a reading
     * @param key - the routing information
     * @param readingParcelMO - the parcel of readings to send
     */
    public void publishReading(ReadingKey key, ReadingParcelMO readingParcelMO);
    
    void start() throws Exception;
}
