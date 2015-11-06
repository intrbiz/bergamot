package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;

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
    public Resource resolve(VirtualCheckExpressionParserContext context)
    {
        Cluster on = this.getCluster().resolve(context);
        return context.lookupResource(on, this.getName());
    }

    public String toString()
    {
        return "resource \"" + this.name + "\" on " + this.getCluster();
    } 
}
