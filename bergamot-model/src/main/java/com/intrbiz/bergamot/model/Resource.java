package com.intrbiz.bergamot.model;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.ResourceCfgAdapter;
import com.intrbiz.bergamot.model.message.ResourceMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A resource of a cluster, which is provided by multiple services
 */
@SQLTable(schema = BergamotDB.class, name = "resource", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "host_name_unq", columns = {"cluster_id", "name"})
public class Resource extends VirtualCheck<ResourceMO, ResourceCfg>
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "configuration", type = "TEXT", adapter = ResourceCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected ResourceCfg configuration;
    
    @SQLColumn(index = 2, name = "cluster_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID clusterId;

    public Resource()
    {
        super();
    }
    
    @Override
    public ResourceCfg getConfiguration()
    {
        return configuration;
    }

    @Override
    public void setConfiguration(ResourceCfg configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void configure(ResourceCfg cfg)
    {
        super.configure(cfg);
        ResourceCfg rcfg = cfg.resolve();
        //
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
        this.description = Util.coalesceEmpty(rcfg.getDescription(), "");
        this.enabled = rcfg.getEnabledBooleanValue();
        this.suppressed = rcfg.getSuppressedBooleanValue();
        // initial state
        // TODO
        /*
        if (rcfg.getInitialState() != null)
        {
            this.getState().setStatus(Status.valueOf(rcfg.getInitialState().getStatus().toUpperCase()));
            this.getState().setOk(this.getState().getStatus().isOk());
            this.getState().setOutput(Util.coalesce(rcfg.getInitialState().getOutput(), ""));
            this.getState().setLastHardStatus(this.getState().getStatus());
            this.getState().setLastHardOk(this.getState().isOk());
            this.getState().setLastHardOutput(this.getState().getOutput());
        }
        */
    }

    @Override
    public String getType()
    {
        return "resource";
    }

    public UUID getClusterId()
    {
        return this.clusterId;
    }

    public void setClusterId(UUID clusterId)
    {
        this.clusterId = clusterId;
    }
    
    public Cluster getCluster()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getCluster(this.getClusterId());
        }
    }
    
    public String toString()
    {
        return "Resource (" + this.id + ") " + this.name + " on cluster " + (this.getCluster() == null ? "null" : this.getCluster().getName());
    }

    @Override
    public ResourceMO toMO(boolean stub)
    {
        ResourceMO mo = new ResourceMO();
        super.toMO(mo, stub);
        if (! stub)
        {
            mo.setCluster(this.getCluster().toStubMO());
        }
        return mo;
    }
}
