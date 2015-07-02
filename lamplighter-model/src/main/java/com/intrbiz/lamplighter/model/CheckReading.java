package com.intrbiz.lamplighter.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLUnique;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.lamplighter.data.LamplighterDB;

/**
 * Readings for a check
 */
@SQLTable(schema = LamplighterDB.class, name = "check_reading", since = @SQLVersion({ 1, 0, 0 }))
@SQLUnique(name = "name_unq", columns = { "check_id", "name" })
public class CheckReading implements Serializable
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    protected UUID id;
    
    @SQLColumn(index = 2, name = "site_id", since = @SQLVersion({ 1, 0, 0 }))
    protected UUID siteId;
    
    @SQLColumn(index = 3, name = "check_id", since = @SQLVersion({ 1, 0, 0 }))
    protected UUID checkId;

    @SQLColumn(index = 4, name = "name", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected String name;

    @SQLColumn(index = 5, name = "summary", since = @SQLVersion({ 1, 0, 0 }))
    protected String summary;

    @SQLColumn(index = 6, name = "description", since = @SQLVersion({ 1, 0, 0 }))
    protected String description;
    
    @SQLColumn(index = 7, name = "unit", since = @SQLVersion({ 1, 0, 0 }))
    protected String unit;
    
    @SQLColumn(index = 8, name = "reading_type", since = @SQLVersion({ 1, 0, 0 }))
    protected String readingType;
    
    @SQLColumn(index = 9, name = "schema", since = @SQLVersion({ 1, 0, 0 }))
    protected String schema;
    
    @SQLColumn(index = 10, name = "table", since = @SQLVersion({ 1, 0, 0 }))
    protected String table;
    
    @SQLColumn(index = 11, name = "created", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp created = new Timestamp(System.currentTimeMillis());

    @SQLColumn(index = 12, name = "updated", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp updated = new Timestamp(System.currentTimeMillis());

    @SQLColumn(index = 13, name = "poll_interval", since = @SQLVersion({ 1, 1, 0 }))
    protected long pollInterval;
    
    public CheckReading()
    {
        super();
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public String getReadingType()
    {
        return readingType;
    }

    public void setReadingType(String readingType)
    {
        this.readingType = readingType;
    }

    public String getSchema()
    {
        return schema;
    }

    public void setSchema(String schema)
    {
        this.schema = schema;
    }

    public String getTable()
    {
        return table;
    }

    public void setTable(String table)
    {
        this.table = table;
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

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public long getPollInterval()
    {
        return this.pollInterval;
    }

    public void setPollInterval(long pollInterval)
    {
        this.pollInterval = pollInterval;
    }
}
