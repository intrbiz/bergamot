package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.util.UUID;

import com.intrbiz.bergamot.config.model.BergamotCfg;

public class ChangeSet implements Serializable
{
    private static final long serialVersionUID = 1L;

    private UUID id;
    
    private String summary;
    
    private String description;
    
    private BergamotCfg configuration;
    
    public ChangeSet()
    {
        super();
    }
    
    public ChangeSet(String summary, String description)
    {
        super();
        this.id = UUID.randomUUID();
        this.summary = summary;
        this.description = description;
        this.configuration = new BergamotCfg();
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public BergamotCfg getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(BergamotCfg configuration)
    {
        this.configuration = configuration;
    }
}
