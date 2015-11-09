package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class ResourceByName implements CheckReference, ResourceReference
{
    private static final long serialVersionUID = 1L;
    
    private ClusterReference cluster;
    
    private String name;
    
    public ResourceByName()
    {
        super();
    }
    
    public ResourceByName(ClusterReference cluster, String name)
    {
        super();
        this.cluster = cluster;
        this.name = name;
    }

    public ClusterReference getCluster()
    {
        return cluster;
    }

    public void setCluster(ClusterReference cluster)
    {
        this.cluster = cluster;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public Resource resolve(VirtualCheckExpressionContext context)
    {
        Cluster on = this.getCluster().resolve(context);
        return context.lookupResource(on, this.getName());
    }

    public String toString()
    {
        return "resource '" + this.name + "' on " + this.getCluster();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ResourceByName other = (ResourceByName) obj;
        if (cluster == null)
        {
            if (other.cluster != null) return false;
        }
        else if (!cluster.equals(other.cluster)) return false;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    } 
}
