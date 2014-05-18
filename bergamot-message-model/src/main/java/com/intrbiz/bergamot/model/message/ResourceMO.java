package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.resource")
public class ResourceMO extends VirtualCheckMO
{
    @JsonProperty("cluster")
    private ClusterMO cluster;
    
    public ResourceMO()
    {
        super();
    }
    
    public String getType()
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
    
    public String toString()
    {
        return "resource { id: " + this.id + "}";
    }
}
