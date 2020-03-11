package com.intrbiz.bergamot.worker.engine;

import com.intrbiz.bergamot.model.message.check.ExecuteCheck;
import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;
import com.intrbiz.bergamot.model.message.result.ActiveResultMO;
import com.intrbiz.bergamot.model.message.result.PassiveResultMO;
import com.intrbiz.bergamot.model.message.result.ResultMO;
import com.intrbiz.gerald.polyakov.Reading;

/**
 * The context that a ExecuteCheck is within
 */
public interface CheckExecutionContext
{    
    /**
     * Publish a result
     */
    void publishResult(ResultMO resultMO);
    
    /**
     * Publish an active result for the given check
     * @param resultMO the active result
     */
    default void publishActiveResult(ActiveResultMO resultMO)
    {
        this.publishResult(resultMO);
    }
    
    /**
     * Publish a passive result for a check of the given site
     * @param resultMO the passive result
     */
    default void publishPassiveResult(PassiveResultMO resultMO)
    {
        this.publishResult(resultMO);
    }
    
    /**
     * Publish a readings
     * @param readingParcelMO the readings all parcelled up and addressed
     */
    void publishReading(ReadingParcelMO readingParcelMO);
    
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
