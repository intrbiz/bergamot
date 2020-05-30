package com.intrbiz.bergamot.cluster.registry.model;

public class RegistryEvent<K, V>
{
    public enum Type { ADDED, REMOVED, UPDATED, DISCONNECTED, CONNECTED };
    
    protected final Type type;
    
    protected final K id;
    
    protected final V data;
    
    public RegistryEvent(Type type, K id, V data)
    {
        super();
        this.type = type;
        this.id = id;
        this.data = data;
    }

    public final Type getType()
    {
        return this.type;
    }

    public final K getId()
    {
        return this.id;
    }

    public final V getData()
    {
        return this.data;
    }
    
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + this.type + ": " + this.id + (data == null ? "" : "\n" + this.data);
    }
}
