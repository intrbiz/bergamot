package com.intrbiz.bergamot.model;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.ClusterCfgAdapter;
import com.intrbiz.bergamot.model.message.ClusterMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A cluster of resources spanning many hosts
 */
@SQLTable(schema = BergamotDB.class, name = "cluster", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = { "site_id", "name" })
public class Cluster extends VirtualCheck<ClusterMO, ClusterCfg>
{   
    @SQLColumn(index = 1, name = "configuration", type = "TEXT", adapter = ClusterCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected ClusterCfg configuration;
    
    public Cluster()
    {
        super();
    }
    
    @Override
    public ClusterCfg getConfiguration()
    {
        return configuration;
    }

    @Override
    public void setConfiguration(ClusterCfg configuration)
    {
        this.configuration = configuration;
    }
    
    @Override
    public void configure(ClusterCfg cfg)
    {
        super.configure(cfg);
        ClusterCfg rcfg = cfg.resolve();
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
        return "cluster";
    }

    public List<Resource> getResources()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getResourcesOnCluster(getId());
        }
    }

    public void addResource(Resource resource)
    {
        // TODO
    }

    public Resource getResource(String name)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            return db.getResourceOnCluster(this.getId(), name);
        }
    }

    @Override
    public ClusterMO toMO(boolean stub)
    {
        ClusterMO mo = new ClusterMO();
        super.toMO(mo, stub);
        if (! stub)
        {
            mo.setResources(this.getResources().stream().map(Resource::toStubMO).collect(Collectors.toList()));
        }
        return mo;
    }
    
    public String toString()
    {
        return "Cluster (" + this.id + ") " + this.name;
    }
}
