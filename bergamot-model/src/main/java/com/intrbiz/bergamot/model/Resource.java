package com.intrbiz.bergamot.model;

import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.data.BergamotDB;
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
    
    @SQLColumn(index = 1, name = "cluster_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID clusterId;
    
    @SQLColumn(index = 2, name = "category", since = @SQLVersion({ 2, 5, 0 }))
    private String category;

    @SQLColumn(index = 3, name = "application", since = @SQLVersion({ 2, 5, 0 }))
    private String application;

    public Resource()
    {
        super();
    }

    @Override
    public void configure(ResourceCfg configuration, ResourceCfg resolvedConfiguration)
    {
        super.configure(configuration, resolvedConfiguration);
        this.category = resolvedConfiguration.getCategory();
        this.application = resolvedConfiguration.getApplication();
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
    
    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getApplication()
    {
        return application;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }
    
    /**
     * Resolve the category tag for this Resource
     * @return the category tag or null is not specified
     */
    public String resolveCategory()
    {
        if (! Util.isEmpty(this.getCategory())) return this.getCategory();
        return null;
    }

    /**
     * Resolve the application tag for this Resource
     * @return the application tag or null is not specified
     */
    public String resolveApplication()
    {
        if (! Util.isEmpty(this.getApplication())) return this.getApplication();
        return null;
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
        mo.setCategory(this.resolveCategory());
        mo.setApplication(this.resolveApplication());
        if (! stub)
        {
            mo.setCluster(this.getCluster().toStubMO());
        }
        return mo;
    }
}
