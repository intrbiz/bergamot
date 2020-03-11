package com.intrbiz.lamplighter.reading;

import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;


/**
 * Process the reading queue and store the readings
 */
public interface ReadingProcessor
{    
    void start();
    
    /**
     * Process the readings of a check which executed
     * @param readings - the readings to process
     */
    void processReadings(ReadingParcelMO readings);
}
