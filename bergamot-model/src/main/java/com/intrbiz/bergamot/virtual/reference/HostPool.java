package com.intrbiz.bergamot.virtual.reference;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class HostPool implements HostPoolReference
{
    private static final long serialVersionUID = 1L;

    private String pool;

    public HostPool()
    {
        super();
    }

    public HostPool(String pool)
    {
        this.pool = pool;
    }

    @Override
    public String getPool()
    {
        return pool;
    }

    public void setPool(String pool)
    {
        this.pool = pool;
    }

    @Override
    public List<HostReference> resolvePool(VirtualCheckExpressionContext context)
    {
        return context.lookupHostsInPool(this.getPool()).stream()
                .map((h) -> new HostById(h.getId())).collect(Collectors.toList());
    }
    
    public String toString()
    {
        return "hosts in pool '" + this.getPool() + "'"; 
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pool == null) ? 0 : pool.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HostPool other = (HostPool) obj;
        if (pool == null)
        {
            if (other.pool != null) return false;
        }
        else if (!pool.equals(other.pool)) return false;
        return true;
    }
}
