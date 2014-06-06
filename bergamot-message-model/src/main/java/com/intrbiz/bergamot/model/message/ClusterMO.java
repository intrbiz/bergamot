package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.cluster")
public class ClusterMO extends VirtualCheckMO
{   
    @JsonProperty("resources")
    private List<ResourceMO> resources = new LinkedList<ResourceMO>();
    
    public ClusterMO()
    {
        super();
    }
    
    public String getCheckType()
    {
        return "cluster";
    }

    public List<ResourceMO> getResources()
    {
        return resources;
    }

    public void setResources(List<ResourceMO> resources)
    {
        this.resources = resources;
    }
}
