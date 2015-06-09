package com.intrbiz.lamplighter.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLVersion;


public abstract class StoredReading implements Serializable
{
    private static final long serialVersionUID = 1L;

    @SQLColumn(index = 1, name = "site_id", since = @SQLVersion({ 1, 0, 0 }))
    protected UUID siteId;
    
    @SQLPrimaryKey()
    @SQLColumn(index = 2, name = "reading_id", since = @SQLVersion({ 1, 0, 0 }))
    protected UUID readingId;
    
    @SQLPrimaryKey()
    @SQLColumn(index = 3, name = "collected_at", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp collectedAt;
    
    public StoredReading()
    {
        super();
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public UUID getReadingId()
    {
        return readingId;
    }

    public void setReadingId(UUID readingId)
    {
        this.readingId = readingId;
    }

    public Timestamp getCollectedAt()
    {
        return collectedAt;
    }

    public void setCollectedAt(Timestamp collectedAt)
    {
        this.collectedAt = collectedAt;
    }
}
