package com.intrbiz.bergamot.config.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

@XmlType(name = "location")
@XmlRootElement(name = "location")
public class LocationCfg extends SecuredObjectCfg<LocationCfg>
{
    private static final long serialVersionUID = 1L;
    
    private String location;
    
    private String workerPool;

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

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}
