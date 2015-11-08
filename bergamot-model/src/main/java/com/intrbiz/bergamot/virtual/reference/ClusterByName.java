package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class ClusterByName implements CheckReference, ClusterReference
{
    private static final long serialVersionUID = 1L;

    private String name;

    public ClusterByName()
    {
        super();
    }

    public ClusterByName(String name)
    {
        this.name = name;
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
    public Cluster resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupCluster(this.getName());
    }
    
    public String toString()
    {
        return "cluster \"" + this.getName() + "\""; 
    }
}
