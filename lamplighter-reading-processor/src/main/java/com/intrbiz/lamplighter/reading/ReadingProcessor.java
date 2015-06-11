package com.intrbiz.lamplighter.reading;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.reading.ReadingParcelMO;


/**
 * Process the reading queue and store the readings
 */
public interface ReadingProcessor
{
    int getThreads();
    
    void setThreads(int threads);
    
    void start();
    
    /**
     * Process the readings of a check which executed
     * @param readings - the readings to process
     */
    void processReadings(ReadingParcelMO readings);
    
    /**
     * Make this reading processor responsible for the given pool
     * @param site the site id
     * @param pool the per site pool id
     */
    void ownPool(UUID site, int pool);
    
    /**
     * Make this reading processor not responsible for the given pool
     * @param site the site id
     * @param pool the per site pool id
     */
    void disownPool(UUID site, int pool);
}
