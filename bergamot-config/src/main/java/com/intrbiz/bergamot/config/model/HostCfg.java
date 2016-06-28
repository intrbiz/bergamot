package com.intrbiz.bergamot.config.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.bergamot.config.resolver.stratergy.MergeList;
import com.intrbiz.bergamot.config.resolver.stratergy.SmartMergeSet;

@XmlType(name = "host")
@XmlRootElement(name = "host")
public class HostCfg extends ActiveCheckCfg<HostCfg>
{
    private static final long serialVersionUID = 1L;

    private List<ServiceCfg> services = new LinkedList<ServiceCfg>();

    private List<TrapCfg> traps = new LinkedList<TrapCfg>();

    private String location;

    private String address;
    
    private Set<String> serviceGroups = new LinkedHashSet<String>();

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
        return this.services.stream().filter((s) -> {
            return name.equals(s.getName());
        }).findFirst().get();
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
        return this.traps.stream().filter((t) -> {
            return name.equals(t.getName());
        }).findFirst().get();
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
    
    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "service-groups")
    @ResolveWith(SmartMergeSet.class)
    public Set<String> getServiceGroups()
    {
        return serviceGroups;
    }

    public void setServiceGroups(Set<String> serviceGroups)
    {
        this.serviceGroups = serviceGroups;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = super.getTemplatedChildObjects();
        if (this.services != null) 
            r.addAll(this.services);
        if (this.traps != null) 
            r.addAll(this.traps);
        return r;
    }

    @Override
    protected void resolveChildren(HostCfg resolved)
    {
        // services
        List<ServiceCfg> services = new LinkedList<ServiceCfg>();
        for (ServiceCfg cfg : resolved.getServices())
        {
            services.add(cfg.resolve());
        }
        resolved.setServices(services);
        // traps
        List<TrapCfg> traps = new LinkedList<TrapCfg>();
        for (TrapCfg cfg : resolved.getTraps())
        {
            traps.add(cfg.resolve());
        }
        resolved.setTraps(traps);
    }
}
