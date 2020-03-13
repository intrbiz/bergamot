package com.intrbiz.bergamot.cluster.coordinator.task;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ProcessorTask implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    public enum Action
    {
        REGISTER,
        DEREGISTER
    }

    private final ProcessorTask.Action action;
    
    private final UUID id;
    
    public ProcessorTask(ProcessorTask.Action action, UUID id)
    {
        super();
        this.action = Objects.requireNonNull(action);
        this.id = Objects.requireNonNull(id);
    }
    
    public ProcessorTask.Action getAction()
    {
        return this.action;
    }

    public UUID getId()
    {
        return this.id;
    }

    public String toString()
    {
        return this.action + " processor " + this.id;
    }
}