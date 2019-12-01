package com.intrbiz.bergamot.scheduler;

import java.util.Collection;
import java.util.UUID;

import com.intrbiz.bergamot.cluster.listener.ProcessingPoolListener;
import com.intrbiz.bergamot.model.ActiveCheck;

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
    
    void schedule(Collection<ActiveCheck<?,?>> check);
    
    void schedulePool(UUID siteId, int processingPool);
    
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
    
    void unschedule(Collection<UUID> check);
    
    void unschedulePool(UUID siteId, int processingPool);
    
    /**
     * Start the scheduler
     * @throws Exception
     */
    void start() throws Exception;

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
}
