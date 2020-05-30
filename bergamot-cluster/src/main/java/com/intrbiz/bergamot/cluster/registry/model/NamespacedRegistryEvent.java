package com.intrbiz.bergamot.cluster.registry.model;

public class NamespacedRegistryEvent<N, K, V> extends RegistryEvent<K, V>
{   
    protected final N namespace;
    
    public NamespacedRegistryEvent(Type type, N namespace, K id, V data)
    {
        super(type, id, data);
        this.namespace = namespace;
    }
    
    public final N getNamespace()
    {
        return this.namespace;
    }

    public String toString()
    {
        return this.getClass().getSimpleName() + " " + this.type + ": " + this.namespace + "/" + this.id + (data == null ? "" : "\n" + this.data);
    }
}
