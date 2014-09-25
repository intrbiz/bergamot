package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.adapter.BergamotCfgAdapter;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A configuration change which has or might be made
 */
@SQLTable(schema = BergamotDB.class, name = "config_change", since = @SQLVersion({ 1, 0, 0 }))
public class ConfigChange implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    protected UUID id;
    
    @SQLColumn(index = 2, name = "site_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT)
    protected UUID siteId;

    @SQLColumn(index = 3, name = "summary", since = @SQLVersion({ 1, 0, 0 }))
    protected String summary;

    @SQLColumn(index = 4, name = "description", since = @SQLVersion({ 1, 0, 0 }))
    protected String description;
    
    @SQLColumn(index = 5, name = "created", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp created = new Timestamp(System.currentTimeMillis());

    @SQLColumn(index = 6, name = "updated", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp updated = new Timestamp(System.currentTimeMillis());
    
    @SQLColumn(index = 7, name = "applied", since = @SQLVersion({ 1, 0, 0 }))
    protected boolean applied = false;

    @SQLColumn(index = 8, name = "configuration", type = "TEXT", adapter = BergamotCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected Configuration configuration;

    public ConfigChange()
    {
        super();
    }

    public ConfigChange(UUID siteId, BergamotCfg configuration)
    {
        super();
        this.siteId = siteId;
        this.id = Site.randomId(siteId);
        if (configuration != null)
        {
            this.summary = configuration.getSummary();
            this.description = configuration.getDescription();
        }
        this.updated = this.created = new Timestamp(System.currentTimeMillis());
        this.applied = false;
        this.configuration = configuration;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
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

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
    }

    public Timestamp getUpdated()
    {
        return updated;
    }

    public void setUpdated(Timestamp updated)
    {
        this.updated = updated;
    }

    public boolean isApplied()
    {
        return applied;
    }

    public void setApplied(boolean applied)
    {
        this.applied = applied;
    }
}
