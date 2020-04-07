package com.intrbiz.bergamot.cluster.election.model;

import java.util.UUID;

public final class ElectionEvent
{
    public enum Type { ADDED };
    
    protected final Type type;
    
    protected final UUID id;
    
    public ElectionEvent(Type type, UUID id)
    {
        super();
        this.type = type;
        this.id = id;
    }

    public Type getType()
    {
        return this.type;
    }

    public UUID getId()
    {
        return this.id;
    }
    
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + this.type + ": " + this.id;
    }
}
