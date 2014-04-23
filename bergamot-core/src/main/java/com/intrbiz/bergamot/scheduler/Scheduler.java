package com.intrbiz.bergamot.scheduler;

import com.intrbiz.bergamot.config.SchedulerCfg;
import com.intrbiz.bergamot.engine.BergamotEngine;
import com.intrbiz.bergamot.model.Checkable;

/**
 * Schedule the execution of stuff, mainly host and service checks
 */
public interface Scheduler extends BergamotEngine<SchedulerCfg>
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
    void schedule(Checkable checkable);
    
    /**
     * Reschedule the given check due to some form of state change
     */
    void reschedule(Checkable checkable);
    
    /**
     * Ensure that the given check is enabled, so that it will be 
     * executed
     */
    void enable(Checkable checkable);
    
    /**
     * Ensure that the given check is disable, so that it will not be 
     * executed
     */
    void disable(Checkable checkable);
}
