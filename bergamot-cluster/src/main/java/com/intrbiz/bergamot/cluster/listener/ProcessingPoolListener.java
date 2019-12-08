package com.intrbiz.bergamot.cluster.listener;

import java.util.UUID;

import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;

/**
 * Listen to decisions from the {@code ProcessingPoolCoordinator}
 */
public interface ProcessingPoolListener
{

    void registerPool(UUID siteId, int processingPool);
    
    void deregisterPool(UUID siteId, int processingPool);
    
    void handleSchedulerAction(SchedulerAction action);
    
}
