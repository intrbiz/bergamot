package com.intrbiz.bergamot.cluster.model;

import java.io.Serializable;
import java.util.UUID;

import com.intrbiz.bergamot.model.Site;

public class ProcessingPoolRegistration implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final UUID site;

    private final int pool;

    private UUID owner;
    
    private UUID previousOwner;

    public ProcessingPoolRegistration(UUID site, int pool)
    {
        super();
        this.site = site;
        this.pool = pool;
    }
    
    public UUID getKey()
    {
        return Site.getSiteProcessingPool(this.site, this.pool);
    }

    public UUID getSite()
    {
        return site;
    }

    public int getPool()
    {
        return pool;
    }

    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    public UUID getOwner()
    {
        return owner;
    }

    public UUID getPreviousOwner()
    {
        return previousOwner;
    }
    
    public void owner(UUID newOwner)
    {
        this.owner = newOwner;
        this.previousOwner = null;
    }
    
    public void migrate(UUID newOwner)
    {
        this.previousOwner = this.owner;
        this.owner = newOwner;
    }
}
