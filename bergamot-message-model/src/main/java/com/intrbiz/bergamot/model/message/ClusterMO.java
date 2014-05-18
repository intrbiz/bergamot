package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.cluster")
public class ClusterMO extends VirtualCheckMO
{    
    public ClusterMO()
    {
        super();
    }
    
    public String getType()
    {
        return "cluster";
    }
    
    public String toString()
    {
        return "cluster { id: " + this.id + "}";
    }
}
