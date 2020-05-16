package com.intrbiz.bergamot.worker.engine;

import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.processor.result.PassiveResult;
import com.intrbiz.bergamot.model.message.processor.result.ResultMessage;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
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
    void publishActiveResult(ActiveResult result);
    
    /**
     * Publish a passive result for a check of the given site
     * @param resultMO the passive result
     */
    void publishPassiveResult(PassiveResult result);
    
    /**
     * Publish a readings
     * @param readingParcelMO the readings all parcelled up and addressed
     */
    void publishReading(ReadingParcelMessage reading);
    
    /**
     * Publish the given readings for the given check execution
     * @param check the check which was executed
     * @param readings the readings
     */
    default void publishReading(Reading... readings)
    {
        ReadingParcelMessage readingParcel = new ReadingParcelMessage().captured(System.currentTimeMillis());
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
    default void publishReading(ExecuteCheck check, Reading... readings)
    {
        ReadingParcelMessage readingParcel = new ReadingParcelMessage().captured(System.currentTimeMillis());
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
        ReadingParcelMessage readingParcel = new ReadingParcelMessage().captured(System.currentTimeMillis());
        for (Reading reading : readings)
        {
            readingParcel.reading(reading);
        }
        this.publishReading(readingParcel);
    }   
}
