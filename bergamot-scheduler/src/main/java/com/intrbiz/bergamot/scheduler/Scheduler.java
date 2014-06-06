package com.intrbiz.bergamot.scheduler;

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
     */
    void reschedule(ActiveCheck<?,?> check);
    
    /**
     * Ensure that the given check is enabled, so that it will be 
     * executed
     */
    void enable(ActiveCheck<?,?> check);
    
    /**
     * Ensure that the given check is disable, so that it will not be 
     * executed
     */
    void disable(ActiveCheck<?,?> check);
    
    /**
     * Start the scheduler
     * @throws Exception
     */
    void start() throws Exception;
}
