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
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT, since = @SQLVersion({ 1, 0, 0 }))
    protected UUID siteId;

    @SQLColumn(index = 3, name = "summary", since = @SQLVersion({ 1, 0, 0 }))
    protected String summary;

    @SQLColumn(index = 4, name = "description", since = @SQLVersion({ 1, 0, 0 }))
    protected String description;
    
    @SQLColumn(index = 5, name = "created", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp created = new Timestamp(System.currentTimeMillis());
    
    @SQLColumn(index = 6, name = "applied", since = @SQLVersion({ 1, 0, 0 }))
    protected boolean applied = false;

    @SQLColumn(index = 7, name = "applied_at", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp appliedAt;

    @SQLColumn(index = 8, name = "configuration", type = "TEXT", adapter = BergamotCfgAdapter.class, since = @SQLVersion({ 1, 0, 0 }))
    protected Configuration configuration;
    
    @SQLColumn(index = 9, name = "created_by_id", since = @SQLVersion({ 3, 10, 0 }))
    @SQLForeignKey(references = Contact.class, on = "id", onDelete = Action.SET_NULL, onUpdate = Action.RESTRICT, since = @SQLVersion({ 3, 10, 0 }))
    protected UUID createdById;
    
    @SQLColumn(index = 10, name = "applied_by_id", since = @SQLVersion({ 3, 10, 0 }))
    @SQLForeignKey(references = Contact.class, on = "id", onDelete = Action.SET_NULL, onUpdate = Action.RESTRICT, since = @SQLVersion({ 3, 10, 0 }))
    protected UUID appliedById;

    public ConfigChange()
    {
        super();
    }

    public ConfigChange(UUID siteId, Contact createdBy, BergamotCfg configuration)
    {
        super();
        this.siteId = siteId;
        this.id = Site.randomId(siteId);
        this.createdById = createdBy == null ? null : createdBy.getId();
        if (configuration != null)
        {
            this.summary = configuration.getSummary();
            this.description = configuration.getDescription();
        }
        this.created = new Timestamp(System.currentTimeMillis());
        this.applied = false;
        this.appliedAt = null;
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

    public boolean isApplied()
    {
        return applied;
    }

    public void setApplied(boolean applied)
    {
        this.applied = applied;
    }

    public Timestamp getAppliedAt()
    {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt)
    {
        this.appliedAt = appliedAt;
    }

    public UUID getCreatedById()
    {
        return createdById;
    }

    public void setCreatedById(UUID createdById)
    {
        this.createdById = createdById;
    }

    public UUID getAppliedById()
    {
        return appliedById;
    }

    public void setAppliedById(UUID appliedById)
    {
        this.appliedById = appliedById;
    }
    
    public Contact getCreatedBy()
    {
        if (this.createdById != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                return db.getContact(this.createdById);
            }
        }
        return null;
    }
    
    public Contact getAppliedBy()
    {
        if (this.appliedById != null)
        {
            try (BergamotDB db = BergamotDB.connect())
            {
                return db.getContact(this.appliedById);
            }
        }
        return null;
    }
}
