package com.intrbiz.bergamot.virtual.reference;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class TrapPool implements TrapPoolReference
{
    private static final long serialVersionUID = 1L;

    private String trap;

    private String pool;

    public TrapPool()
    {
        super();
    }

    public TrapPool(String trap, String pool)
    {
        this.trap = trap;
        this.pool = pool;
    }

    public String getTrap()
    {
        return trap;
    }

    public void setTrap(String trap)
    {
        this.trap = trap;
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
    public List<TrapReference> resolvePool(VirtualCheckExpressionContext context)
    {
        return context.lookupTrapsInPool(this.trap, this.getPool()).stream().map((s) -> new TrapById(s.getId())).collect(Collectors.toList());
    }

    public String toString()
    {
        return "trap '" + this.getTrap() + "' on hosts in pool '" + this.getPool() + "'";
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
        TrapPool other = (TrapPool) obj;
        if (pool == null)
        {
            if (other.pool != null) return false;
        }
        else if (!pool.equals(other.pool)) return false;
        return true;
    }
}
