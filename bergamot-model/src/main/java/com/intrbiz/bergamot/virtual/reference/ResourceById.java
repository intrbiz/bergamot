package com.intrbiz.bergamot.virtual.reference;

import java.util.UUID;

import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class ResourceById extends CheckById implements ResourceReference
{
    private static final long serialVersionUID = 1L;
    
    public ResourceById()
    {
        super();
    }

    public ResourceById(UUID id)
    {
        super(id);
    }
    
    @Override
    public Resource resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupResource(this.getId());
    }
    
    public String toString()
    {
        return "resource " + this.getId();
    }
}
