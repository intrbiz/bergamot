package com.intrbiz.bergamot.config.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.MergeList;

@XmlType(name = "cluster")
@XmlRootElement(name = "cluster")
public class ClusterCfg extends VirtualCheckCfg<ClusterCfg>
{
    private static final long serialVersionUID = 1L;
    
    private List<ResourceCfg> resources = new LinkedList<ResourceCfg>();

    public ClusterCfg()
    {
        super();
    }

    @XmlElementRef(type = ResourceCfg.class)
    @ResolveWith(MergeList.class)
    public List<ResourceCfg> getResources()
    {
        return resources;
    }

    public void setResources(List<ResourceCfg> resources)
    {
        this.resources = resources;
    }
    
    public ResourceCfg lookupResource(String name)
    {
        return this.resources.stream().filter((r) -> { return name.equals(r.getName()); }).findFirst().get();
    }
    
    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = super.getTemplatedChildObjects();
        if (this.resources != null)
        {
            for (ResourceCfg resource : this.resources)
            {
                r.add(resource);
            }
        }
        return r;
    }

    @Override
    protected void resolveChildren(ClusterCfg resolved)
    {
        List<ResourceCfg> resources = new LinkedList<ResourceCfg>();
        for (ResourceCfg cfg : resolved.getResources())
        {
            resources.add(cfg.resolve());
        }
        resolved.setResources(resources);
    }
}