package com.intrbiz.bergamot.virtual.reference;

import java.util.UUID;

import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class ClusterById extends CheckById implements ClusterReference
{
    private static final long serialVersionUID = 1L;
    
    public ClusterById()
    {
        super();
    }

    public ClusterById(UUID id)
    {
        super(id);
    }
    
    @Override
    public Cluster resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupCluster(this.getId());
    }
    
    public String toString()
    {
        return "cluster " + this.getId();
    }
}
