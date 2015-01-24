package com.intrbiz.bergamot.scheduler;

import java.util.UUID;

import com.intrbiz.bergamot.model.ActiveCheck;

/**
 * Schedule the execution of stuff, mainly host and service checks
 */
public interface Scheduler
{
    /**
     * Pause scheduling all checks
     */
    void pause();
    
    /**
     * Resume scheduling all checks
     */
    void resume();
    
    /**
     * Schedule the given check
     */
    void schedule(ActiveCheck<?,?> check);
    
    /**
     * Reschedule the given check due to some form of state change
     * @param interval - the new interval of the check, optional if <= 0
     */
    void reschedule(ActiveCheck<?,?> check, long interval);
    
    /**
     * Ensure that the given check is enabled, so that it will be 
     * executed
     */
    void enable(UUID check);
    
    /**
     * Ensure that the given check is disable, so that it will not be 
     * executed
     */
    void disable(UUID check);
    
    /**
     * Remove the given check from the scheduler
     */
    void unschedule(UUID check);
    
    /**
     * Make this scheduler responsible for the given pool
     * @param site the site id
     * @param pool the per site pool id
     */
    void ownPool(UUID site, int pool);
    
    /**
     * Make this scheduler not responsible for the given pool
     * @param site the site id
     * @param pool the per site pool id
     */
    void disownPool(UUID site, int pool);
    
    /**
     * Start the scheduler
     * @throws Exception
     */
    void start() throws Exception;
}
