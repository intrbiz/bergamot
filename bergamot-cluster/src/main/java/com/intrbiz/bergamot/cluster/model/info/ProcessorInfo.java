package com.intrbiz.bergamot.cluster.model.info;

import java.util.LinkedList;
import java.util.List;

public class ProcessorInfo
{
    private String uuid;

    private String address;

    private List<PoolInfo> pools = new LinkedList<PoolInfo>();

    public ProcessorInfo()
    {
        super();
    }

    public ProcessorInfo(String uuid, String address)
    {
        super();
        this.uuid = uuid;
        this.address = address;
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
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
