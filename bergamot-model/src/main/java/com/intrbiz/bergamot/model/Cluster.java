package com.intrbiz.bergamot.model;

import java.util.List;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.message.ClusterMO;
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
    private static final long serialVersionUID = 1L;
    
    public Cluster()
    {
        super();
    }
    
    @Override
    public void configure(ClusterCfg configuration, ClusterCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
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
        try (BergamotDB db = BergamotDB.connect())
        {
            db.addResourceToCluster(this, resource);
        }
    }
    
    public void removeResource(Resource resource)
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            db.removeResourceFromCluster(this, resource);
        }
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
