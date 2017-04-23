package com.intrbiz.bergamot.ui.model.health;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClusterHealth
{
    @JsonProperty("members")
    private int members;
    
    @JsonProperty("processing_pools")
    private int processingPools;

    public ClusterHealth()
    {
        super();
    }

    public int getMembers()
    {
        return members;
    }

    public void setMembers(int members)
    {
        this.members = members;
    }

    public int getProcessingPools()
    {
        return processingPools;
    }

    public void setProcessingPools(int processingPools)
    {
        this.processingPools = processingPools;
    }
}
