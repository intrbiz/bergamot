package com.intrbiz.lamplighter.reading;

import com.intrbiz.bergamot.model.message.processor.reading.ReadingParcelMO;


/**
 * Process the reading queue and store the readings
 */
public interface ReadingProcessor
{    
    void start();
    
    void shutdown();
    
    /**
     * Process the readings of a check which executed
     * @param readings - the readings to process
     */
    void process(ReadingParcelMO readings);
}
