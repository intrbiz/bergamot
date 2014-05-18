package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.configuration.Configurable;

/**
 * A cluster of resources spanning many hosts
 */
public class Cluster extends VirtualCheck implements Configurable<ClusterCfg>
{
    private Map<String, Resource> resources = new TreeMap<String, Resource>();
    
    private ClusterCfg config;
    
    public Cluster()
    {
        super();
    }
    
    @Override
    public void configure(ClusterCfg cfg)
    {
        this.config = cfg;
        ClusterCfg rcfg = cfg.resolve();
        //
        this.name = rcfg.getName();
        this.displayName = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.enabled = rcfg.getEnabledBooleanValue();
        this.suppressed = rcfg.getSuppressedBooleanValue();
        // initial state
        if (rcfg.getInitialState() != null)
        {
            this.getState().setStatus(Status.valueOf(rcfg.getInitialState().getStatus().toUpperCase()));
            this.getState().setOk(this.getState().getStatus().isOk());
            this.getState().setOutput(Util.coalesce(rcfg.getInitialState().getOutput(), ""));
            this.getState().setLastHardStatus(this.getState().getStatus());
            this.getState().setLastHardOk(this.getState().isOk());
            this.getState().setLastHardOutput(this.getState().getOutput());
        }
    }

    @Override
    public ClusterCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public String getType()
    {
        return "cluster";
    }
    
    public Set<String> getResourceNames()
    {
        return this.resources.keySet();
    }

    public Collection<Resource> getResources()
    {
        return this.resources.values();
    }

    public void addResource(Resource resource)
    {
        this.resources.put(resource.getName(), resource);
        resource.setCluster(this);
    }

    public Resource getResource(String name)
    {
        return this.resources.get(name);
    }

    public boolean containsResource(String name)
    {
        return this.resources.containsKey(name);
    }

    public int getResourceCount()
    {
        return this.resources.size();
    }

    @Override
    public ClusterMO toMO()
    {
        ClusterMO mo = new ClusterMO();
        super.toMO(mo);
        return mo;
    }
    
    public String toString()
    {
        return "Cluster (" + this.id + ") " + this.name;
    }
}
