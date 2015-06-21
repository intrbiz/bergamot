package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.resource")
public class ResourceMO extends VirtualCheckMO
{
    @JsonProperty("cluster")
    private ClusterMO cluster;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("application")
    private String application;
    
    public ResourceMO()
    {
        super();
    }
    
    public String getCheckType()
    {
        return "resource";
    }

    public ClusterMO getCluster()
    {
        return cluster;
    }

    public void setCluster(ClusterMO cluster)
    {
        this.cluster = cluster;
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
}
