package com.intrbiz.bergamot.worker.engine;

import java.util.UUID;

import com.intrbiz.bergamot.config.ExecutorCfg;
import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.bergamot.queue.key.ActiveResultKey;
import com.intrbiz.bergamot.queue.key.PassiveResultKey;
import com.intrbiz.bergamot.queue.key.ReadingKey;
import com.intrbiz.bergamot.queue.key.ResultKey;
import com.intrbiz.bergamot.worker.engine.script.ActiveCheckScriptContext;
import com.intrbiz.configuration.Configurable;
import com.intrbiz.gerald.polyakov.Reading;

/**
 * Executors execute a particular task.
 * 
 * Note: executors must be thread safe!
 */
public interface Executor<T extends Engine> extends Configurable<ExecutorCfg>
{
    T getEngine();
    
    void setEngine(T engine);
    
    void start() throws Exception;
    
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
    void execute(ExecuteCheck executeCheck);
    
    /**
     * Publish a result
     */
    void publishResult(ResultKey key, ResultMO resultMO);
    
    /**
     * Publish an active result for the given check
     * @param check the check which this result is for
     * @param resultMO the active result
     */
    default void publishActiveResult(ExecuteCheck check, ActiveResultMO resultMO)
    {
        this.publishResult(new ActiveResultKey(check.getSiteId(), check.getProcessingPool()), resultMO);
    }
    
    /**
     * Publish a passive result for a check of the given site
     * @param siteId the site id
     * @param resultMO the passive result
     */
    default void publishPassiveResult(UUID siteId, PassiveResultMO resultMO)
    {
        this.publishResult(new PassiveResultKey(siteId), resultMO);
    }
    
    /**
     * Publish a reading
     * @param key - the routing information
     * @param readingParcelMO - the parcel of readings to send
     */
    void publishReading(ReadingKey key, ReadingParcelMO readingParcelMO);
    
    /**
     * Publish a reading for the given check execution
     * @param check the check which was executed
     * @param readingParcelMO the readings all parcelled up and addressed
     */
    default void publishReading(ExecuteCheck check, ReadingParcelMO readingParcelMO)
    {
        this.publishReading(new ReadingKey(check.getSiteId(), check.getProcessingPool()), readingParcelMO);
    }
    
    /**
     * Publish the given readings for the given check execution
     * @param check the check which was executed
     * @param readings the readings
     */
    default void publishReading(ExecuteCheck check, Reading... readings)
    {
        ReadingParcelMO readingParcel = new ReadingParcelMO().fromCheck(check.getCheckId()).captured(System.currentTimeMillis());
        for (Reading reading : readings)
        {
            readingParcel.reading(reading);
        }
        this.publishReading(check, readingParcel);
    }
    
    /**
     * Publish a readings for a check of the given site
     * @param siteId the site which these readings are fore
     * @param readingParcelMO the readings all parcelled up and addressed
     */
    default void publishReading(UUID siteId, ReadingParcelMO readingParcelMO)
    {
        this.publishReading(new ReadingKey(siteId), readingParcelMO);
    }
    
    /**
     * Create a script context for the given check
     * @param check the check which will be executed
     * @return a script context
     */
    default ActiveCheckScriptContext createScriptContext(ExecuteCheck check)
    {
        return new ActiveCheckScriptContext(check, this);
    }
}
