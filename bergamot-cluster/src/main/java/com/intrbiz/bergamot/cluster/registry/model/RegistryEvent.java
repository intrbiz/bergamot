package com.intrbiz.bergamot.cluster.registry.model;

public final class RegistryEvent<K, V>
{
    public enum Type { ADDED, REMOVED, UPDATED };
    
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

    public Type getType()
    {
        return this.type;
    }

    public K getId()
    {
        return this.id;
    }

    public V getData()
    {
        return this.data;
    }
    
    public String toString()
    {
        return this.getClass().getSimpleName() + " " + this.type + ": " + this.id + (data == null ? "" : "\n" + this.data);
    }
}
