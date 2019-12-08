package com.intrbiz.bergamot.cluster.queue;

import java.util.Objects;
import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.coordinator.ProcessingPoolCoordinator;
import com.intrbiz.bergamot.cluster.coordinator.task.ProcessingPoolTask;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;

public class SchedulerActionProducer
{
    private final HazelcastInstance hazelcastInstance;
    
    private final ProcessingPoolCoordinator coordinator;
    
    public SchedulerActionProducer(HazelcastInstance hazelcastInstance, ProcessingPoolCoordinator coordinator)
    {
        super();
        this.hazelcastInstance = Objects.requireNonNull(hazelcastInstance);
        this.coordinator = Objects.requireNonNull(coordinator);
    }
    
    // Result handling
    
    private IQueue<ProcessingPoolTask> getMigrationQueue(UUID memberUUID)
    {
        return this.hazelcastInstance.getQueue(ObjectNames.buildClusterMigrationQueueName(memberUUID));
    }
    
    public boolean publishSchedulerAction(SchedulerAction action)
    {
        UUID pool = this.coordinator.routeActiveCheck(action.getCheck());
        IQueue<ProcessingPoolTask> queue = this.getMigrationQueue(pool);
        try
        {
            queue.put(new ProcessingPoolTask(ProcessingPoolTask.Action.FIRE, Site.getSiteId(action.getCheck()), Site.computeSiteProcessingPool(action.getCheck()), action));
        }
        catch (Exception e)
        {
        }
        return false;
    }
}
