package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class HostByName implements CheckReference, HostReference
{
    private static final long serialVersionUID = 1L;

    private String name;

    public HostByName()
    {
        super();
    }

    public HostByName(String name)
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
    public Host resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupHost(this.getName());
    }
    
    public String toString()
    {
        return "host \"" + this.getName() + "\""; 
    }
}
