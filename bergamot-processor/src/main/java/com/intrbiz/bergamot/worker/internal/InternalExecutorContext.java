package com.intrbiz.bergamot.worker.internal;

import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMessage;
import com.intrbiz.bergamot.model.message.processor.result.ActiveResult;
import com.intrbiz.bergamot.model.message.worker.check.ExecuteCheck;
import com.intrbiz.bergamot.processor.Locator;
import com.intrbiz.gerald.polyakov.Reading;

public interface InternalExecutorContext
{
    Locator getLocator();
    
    /**
     * Publish an active result for the given check
     * @param resultMO the active result
     */
    void publishResult(ActiveResult result);
    
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
