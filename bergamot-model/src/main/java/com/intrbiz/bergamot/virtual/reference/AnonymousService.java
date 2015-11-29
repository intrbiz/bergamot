package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

/**
 * A reference to a service on a host, but the host is not known
 */
public class AnonymousService implements CheckReference, ServiceReference
{
    private static final long serialVersionUID = 1L;

    private String name;

    public AnonymousService()
    {
        super();
    }

    public AnonymousService(String name)
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
    public Service resolve(VirtualCheckExpressionContext context)
    {
        return context.lookupAnonymousService(this.getName());
    }

    public String toString()
    {
        return "service '" + this.name + "'";
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
        AnonymousService other = (AnonymousService) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }
}
