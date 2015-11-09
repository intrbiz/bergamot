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
        return "trap '" + this.name + "' on " + this.host;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TrapByName other = (TrapByName) obj;
        if (host == null)
        {
            if (other.host != null) return false;
        }
        else if (!host.equals(other.host)) return false;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}
