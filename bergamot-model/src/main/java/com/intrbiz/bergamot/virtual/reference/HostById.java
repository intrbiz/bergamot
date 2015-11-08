package com.intrbiz.bergamot.virtual.reference;

import java.util.UUID;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class HostById extends CheckById implements HostReference
{
    private static final long serialVersionUID = 1L;
    
    public HostById()
    {
        super();
    }

    public HostById(UUID id)
    {
        super(id);
    }
    
    @Override
    public Host resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupHost(this.getId());
    }
    
    public String toString()
    {
        return "host " + this.getId();
    }
}
