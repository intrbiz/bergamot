package com.intrbiz.bergamot.config.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

@XmlType(name = "location")
@XmlRootElement(name = "location")
public class LocationCfg extends NamedObjectCfg<LocationCfg>
{
    private static final long serialVersionUID = 1L;
    
    private String location;
    
    private String workerPool;
    
    private Set<String> securityDomains = new LinkedHashSet<String>();

    public LocationCfg()
    {
        super();
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
    
    @XmlAttribute(name = "worker-pool")
    @ResolveWith(CoalesceEmptyString.class)
    public String getWorkerPool()
    {
        return workerPool;
    }

    public void setWorkerPool(String workerPool)
    {
        this.workerPool = workerPool;
    }
    
    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "security-domains")
    @ResolveWith(CoalesceEmptyCollection.class)
    public Set<String> getSecurityDomains()
    {
        return securityDomains;
    }

    public void setSecurityDomains(Set<String> securityDomains)
    {
        this.securityDomains = securityDomains;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}
