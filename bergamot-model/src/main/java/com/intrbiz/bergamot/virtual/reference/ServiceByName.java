package com.intrbiz.bergamot.virtual.reference;

import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;

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
    public Service resolve(VirtualCheckExpressionParserContext context)
    {
        Host on = (Host) this.getHost().resolve(context);
        return context.lookupService(on, this.getName());
    }

    public String toString()
    {
        return "service \"" + this.name + "\" on " + this.host;
    } 
}
