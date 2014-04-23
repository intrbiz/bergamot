package com.intrbiz.bergamot.scheduler;

import com.intrbiz.bergamot.component.BergamotComponent;
import com.intrbiz.bergamot.config.SchedulerCfg;
import com.intrbiz.bergamot.model.Check;

/**
 * Schedule the execution of stuff, mainly host and service checks
 */
public interface Scheduler extends BergamotComponent<SchedulerCfg>
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
    void schedule(Check check);
    
    /**
     * Reschedule the given check due to some form of state change
     */
    void reschedule(Check check);
    
    /**
     * Ensure that the given check is enabled, so that it will be 
     * executed
     */
    void enable(Check check);
    
    /**
     * Ensure that the given check is disable, so that it will not be 
     * executed
     */
    void disable(Check check);
}
