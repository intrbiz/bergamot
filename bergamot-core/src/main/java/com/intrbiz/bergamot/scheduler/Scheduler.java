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
     * Schedule the given checkable
     */
    void schedule(Checkable checkable);
    
    /**
     * Reschedule the given checkable due to some form of state change
     */
    void reschedule(Checkable checkable);
}
