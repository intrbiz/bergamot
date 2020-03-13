package com.intrbiz.bergamot.scheduler;

import java.util.Collection;
import java.util.UUID;

import com.intrbiz.bergamot.cluster.listener.ProcessingPoolListener;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.message.scheduler.DisableCheck;
import com.intrbiz.bergamot.model.message.scheduler.EnableCheck;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.model.message.scheduler.UnscheduleCheck;

/**
 * Schedule the execution of stuff, mainly host and service checks
 */
public interface Scheduler extends ProcessingPoolListener
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
    
    default void schedule(Collection<ActiveCheck<?,?>> checks)
    {
        for (ActiveCheck<?,?> check : checks)
        {
            this.schedule(check);
        }
    }
    
    void schedulePool(UUID siteId, int processingPool);
    
    void schedule(UUID checkId);
    
    /**
     * Reschedule the given check due to some form of state change
     * @param interval - the new interval of the check, optional if <= 0
     */
    void reschedule(ActiveCheck<?,?> check, long interval);
    
    default void reschedule(ActiveCheck<?,?> check)
    {
        this.unschedule(check);
        this.schedule(check);
    }
    
    default void reschedule(UUID check) {
        this.unschedule(check);
        this.schedule(check);
    }
    
    /**
     * Ensure that the given check is enabled, so that it will be 
     * executed
     */
    void enable(UUID check);
    
    default void enable(ActiveCheck<?,?> check)
    {
        this.enable(check.getId());
    }
    
    /**
     * Ensure that the given check is disable, so that it will not be 
     * executed
     */
    void disable(UUID check);
    
    default void disable(ActiveCheck<?,?> check)
    {
        this.disable(check.getId());
    }
    
    /**
     * Remove the given check from the scheduler
     */
    void unschedule(UUID check);
    
    default void unschedule(ActiveCheck<?,?> check)
    {
        this.unschedule(check.getId());
    }
    
    default void unschedule(Collection<UUID> checks)
    {
        for (UUID check : checks)
        {
            this.unschedule(check);
        }
    }
    
    void unschedulePool(UUID siteId, int processingPool);
    
    /**
     * Start the scheduler
     * @throws Exception
     */
    void start() throws Exception;
    
    void stop();

    @Override
    default void registerPool(UUID siteId, int processingPool)
    {
        this.schedulePool(siteId, processingPool);
    }

    @Override
    default void deregisterPool(UUID siteId, int processingPool)
    {
        this.unschedulePool(siteId, processingPool);
    }

    @Override
    default void handleSchedulerAction(SchedulerAction action)
    {
        if (action instanceof ScheduleCheck)
        {
            this.schedule(action.getCheck());
        }
        else if (action instanceof RescheduleCheck)
        {
            this.reschedule(action.getCheck());
        }
        else if (action instanceof UnscheduleCheck)
        {
            this.unschedule(action.getCheck());
        }
        else if (action instanceof EnableCheck)
        {
            this.enable(action.getCheck());
        }
        else if (action instanceof DisableCheck)
        {
            this.disable(action.getCheck());
        }
    }
}
