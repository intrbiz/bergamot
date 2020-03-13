package com.intrbiz.bergamot.cluster.model.info;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ProcessorInfo
{
    private UUID uuid;

    private String address;

    private List<PoolInfo> pools = new LinkedList<PoolInfo>();

    public ProcessorInfo()
    {
        super();
    }

    public ProcessorInfo(UUID uuid, String address)
    {
        super();
        this.uuid = uuid;
        this.address = address;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public void setUuid(UUID uuid)
    {
        this.uuid = uuid;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public List<PoolInfo> getPools()
    {
        return pools;
    }

    public void setPools(List<PoolInfo> pools)
    {
        this.pools = pools;
    }
}
