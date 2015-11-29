package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

/**
 * A reference to a trap on a host, but the host is not known
 */
public class AnonymousTrap implements CheckReference, TrapReference
{
    private static final long serialVersionUID = 1L;

    private String name;

    public AnonymousTrap()
    {
        super();
    }

    public AnonymousTrap(String name)
    {
        super();
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
    public Trap resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupAnonymousTrap(this.getName());
    }

    public String toString()
    {
        return "trap '" + this.name + "'";
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
        AnonymousTrap other = (AnonymousTrap) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}
