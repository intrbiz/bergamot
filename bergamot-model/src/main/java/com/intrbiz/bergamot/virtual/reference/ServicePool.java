package com.intrbiz.bergamot.virtual.reference;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class ServicePool implements ServicePoolReference
{
    private static final long serialVersionUID = 1L;

    private String service;

    private String pool;

    public ServicePool()
    {
        super();
    }

    public ServicePool(String service, String pool)
    {
        this.service = service;
        this.pool = pool;
    }

    public String getService()
    {
        return service;
    }

    public void setService(String service)
    {
        this.service = service;
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
    public List<ServiceReference> resolvePool(VirtualCheckExpressionContext context)
    {
        return context.lookupServicesInPool(this.service, this.getPool()).stream().map((s) -> new ServiceById(s.getId())).collect(Collectors.toList());
    }

    public String toString()
    {
        return "service '" + this.getService() + "' on hosts in pool '" + this.getPool() + "'";
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
        ServicePool other = (ServicePool) obj;
        if (pool == null)
        {
            if (other.pool != null) return false;
        }
        else if (!pool.equals(other.pool)) return false;
        return true;
    }
}
