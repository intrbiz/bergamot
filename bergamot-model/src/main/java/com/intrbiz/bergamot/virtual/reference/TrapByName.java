package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class TrapByName implements CheckReference, TrapReference
{
    private static final long serialVersionUID = 1L;
    
    private HostReference host;
    
    private String name;
    
    public TrapByName()
    {
        super();
    }
    
    public TrapByName(HostReference host, String name)
    {
        super();
        this.host = host;
        this.name = name;
    }

    public HostReference getHost()
    {
        return host;
    }

    public void setHost(HostReference host)
    {
        this.host = host;
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
    public Trap resolve(VirtualCheckExpressionContext context)
    {
        Host on = (Host) this.getHost().resolve(context);
        return context.lookupTrap(on, this.getName());
    }

    public String toString()
    {
        return "trap \"" + this.name + "\" on " + this.host;
    } 
}
