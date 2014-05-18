package com.intrbiz.bergamot.model;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.configuration.Configurable;

/**
 * A resource of a cluster, which is provided by multiple services
 */
public class Resource extends VirtualCheck implements Configurable<ResourceCfg>
{
    private Cluster cluster;

    private ResourceCfg config;

    public Resource()
    {
        super();
    }

    @Override
    public void configure(ResourceCfg cfg)
    {
        this.config = cfg;
        ResourceCfg rcfg = cfg.resolve();
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
    public ResourceCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public String getType()
    {
        return "resource";
    }

    @Override
    public ResourceMO toMO()
    {
        ResourceMO mo = new ResourceMO();
        super.toMO(mo);
        mo.setCluster(this.getCluster().toMO());
        return mo;
    }

    public Cluster getCluster()
    {
        return cluster;
    }

    public void setCluster(Cluster cluster)
    {
        this.cluster = cluster;
    }
    
    public String toString()
    {
        return "Resource (" + this.id + ") " + this.name + " on cluster " + (this.getCluster() == null ? "null" : this.getCluster().getName());
    }
}
