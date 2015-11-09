package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionContext;

public class ServiceByName implements CheckReference, ServiceReference
{
    private static final long serialVersionUID = 1L;
    
    private HostReference host;
    
    private String name;
    
    public ServiceByName()
    {
        super();
    }
    
    public ServiceByName(HostReference host, String name)
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
    public Service resolve(VirtualCheckExpressionContext context)
    {
        Host on = (Host) this.getHost().resolve(context);
        return context.lookupService(on, this.getName());
    }

    public String toString()
    {
        return "service '" + this.name + "' on " + this.host;
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
        ServiceByName other = (ServiceByName) obj;
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
