package com.intrbiz.bergamot.virtual.reference;

import java.util.UUID;

import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class ServiceById extends CheckById implements ServiceReference
{
    private static final long serialVersionUID = 1L;
    
    public ServiceById()
    {
        super();
    }

    public ServiceById(UUID id)
    {
        super(id);
    }
    
    @Override
    public Service resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupService(this.getId());
    }
    
    public String toString()
    {
        return "service " + this.getId();
    }
}
