package com.intrbiz.bergamot.cluster.model.info;

import java.util.UUID;

public class PoolInfo
{
    private UUID siteId;
    
    private int pool;
    
    public PoolInfo()
    {
        super();
    }

    public PoolInfo(UUID siteId, int pool)
    {
        super();
        this.siteId = siteId;
        this.pool = pool;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public int getPool()
    {
        return pool;
    }

    public void setPool(int pool)
    {
        this.pool = pool;
    }
}
