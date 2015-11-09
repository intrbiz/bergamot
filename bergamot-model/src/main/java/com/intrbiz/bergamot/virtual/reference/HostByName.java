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
        return "host '" + this.getName() + "'"; 
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HostByName other = (HostByName) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}
