package com.intrbiz.bergamot.cluster;

import java.io.Serializable;
import java.util.UUID;

public class ProcessingPool implements Serializable
{
    private static final long serialVersionUID = 1L;

    private UUID site;

    private int pool;

    private String owner;
    
    private String previousOwner;

    public ProcessingPool()
    {
        super();
    }

    public ProcessingPool(UUID site, int pool)
    {
        super();
        this.site = site;
        this.pool = pool;
    }
    
    public String getKey()
    {
        return this.site.toString() + "." + this.pool;
    }

    public UUID getSite()
    {
        return site;
    }

    public void setSite(UUID site)
    {
        this.site = site;
    }

    public int getPool()
    {
        return pool;
    }

    public void setPool(int pool)
    {
        this.pool = pool;
    }

    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public String getPreviousOwner()
    {
        return previousOwner;
    }

    public void setPreviousOwner(String previousOwner)
    {
        this.previousOwner = previousOwner;
    }
    
    public void migrate(String newOwner)
    {
        this.previousOwner = this.owner;
        this.owner = newOwner;
    }
}
