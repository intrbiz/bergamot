package com.intrbiz.bergamot.cluster.coordinator.task;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;

public class ProcessingPoolTask implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public enum Action
    {
        REGISTER,
        DEREGISTER,
        FIRE
    }

    private final ProcessingPoolTask.Action action;
    
    private final UUID site;
    
    private final int pool;
    
    private final SchedulerAction schedulerAction;
    
    public ProcessingPoolTask(ProcessingPoolTask.Action action, UUID site, int pool, SchedulerAction schedulerAction)
    {
        super();
        this.action = Objects.requireNonNull(action);
        this.site = Objects.requireNonNull(site);
        this.pool = Objects.requireNonNull(pool);
        this.schedulerAction = schedulerAction;
    }
    
    public ProcessingPoolTask(ProcessingPoolTask.Action action, UUID site, int pool)
    {
        this(action, site, pool, null);
    }

    public UUID getSite()
    {
        return site;
    }

    public int getPool()
    {
        return pool;
    }
    
    public ProcessingPoolTask.Action getAction()
    {
        return this.action;
    }
    
    public SchedulerAction getSchedulerAction()
    {
        return this.schedulerAction;
    }
    
    public String toString()
    {
        return this.action + " pool " + this.site + "." + this.pool;
    }
}