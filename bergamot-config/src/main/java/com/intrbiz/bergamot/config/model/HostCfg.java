package com.intrbiz.bergamot.config.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.bergamot.config.resolver.stratergy.MergeList;

@XmlType(name = "host")
@XmlRootElement(name = "host")
public class HostCfg extends ActiveCheckCfg<HostCfg>
{
    private List<ServiceCfg> services = new LinkedList<ServiceCfg>();

    private List<TrapCfg> traps = new LinkedList<TrapCfg>();

    private String location;

    private String address;

    public HostCfg()
    {
        super();
    }

    @XmlElementRef(type = ServiceCfg.class)
    @ResolveWith(MergeList.class)
    public List<ServiceCfg> getServices()
    {
        return services;
    }

    public void setServices(List<ServiceCfg> services)
    {
        this.services = services;
    }
    
    public ServiceCfg lookupService(String name)
    {
        return this.services.stream().filter((s) -> { return name.equals(s.getName()); }).findFirst().get();
    }

    @XmlElementRef(type = TrapCfg.class)
    @ResolveWith(MergeList.class)
    public List<TrapCfg> getTraps()
    {
        return traps;
    }

    public void setTraps(List<TrapCfg> traps)
    {
        this.traps = traps;
    }
    
    public TrapCfg lookupTrap(String name)
    {
        return this.traps.stream().filter((t) -> { return name.equals(t.getName()); }).findFirst().get();
    }

    @XmlAttribute(name = "location")
    @ResolveWith(CoalesceEmptyString.class)
    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    @XmlAttribute(name = "address")
    @ResolveWith(CoalesceEmptyString.class)
    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = super.getTemplatedChildObjects();
        if (this.services != null)
        {
            for (ServiceCfg service : this.services)
            {
                r.add(service);
            }
        }
        if (this.traps != null)
        {
            for (TrapCfg trap : this.traps)
            {
                r.add(trap);
            }
        }
        return r;
    }
}
