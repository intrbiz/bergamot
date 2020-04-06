package com.intrbiz.bergamot.worker.engine;

import com.intrbiz.bergamot.model.message.pool.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.pool.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.pool.result.ActiveResult;
import com.intrbiz.bergamot.model.message.pool.result.PassiveResult;
import com.intrbiz.bergamot.model.message.pool.result.ResultMessage;
import com.intrbiz.gerald.polyakov.Reading;

/**
 * The context that a ExecuteCheck is within
 */
public interface CheckExecutionContext
{    
    /**
     * Publish a result
     */
    void publishResult(ResultMessage result);
    
    /**
     * Publish an active result for the given check
     * @param resultMO the active result
     */
    default void publishActiveResult(ActiveResult result)
    {
        this.publishResult(result);
    }
    
    /**
     * Publish a passive result for a check of the given site
     * @param resultMO the passive result
     */
    default void publishPassiveResult(PassiveResult result)
    {
        this.publishResult(result);
    }
    
    /**
     * Publish a readings
     * @param readingParcelMO the readings all parcelled up and addressed
     */
    void publishReading(ReadingParcelMO reading);
    
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
        this.publishReading(readingParcel);
    }
    
    /**
     * Publish the given readings for the given check execution
     * @param check the check which was executed
     * @param readings the readings
     */
    default void publishReading(ExecuteCheck check, Iterable<Reading> readings)
    {
        ReadingParcelMO readingParcel = new ReadingParcelMO().fromCheck(check.getCheckId()).captured(System.currentTimeMillis());
        for (Reading reading : readings)
        {
            readingParcel.reading(reading);
        }
        this.publishReading(readingParcel);
    }   
}
