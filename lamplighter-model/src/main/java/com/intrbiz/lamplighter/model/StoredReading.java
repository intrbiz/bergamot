package com.intrbiz.lamplighter.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.data.db.compiler.meta.PartitionMode;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLIndex;
import com.intrbiz.data.db.compiler.meta.SQLPartition;
import com.intrbiz.data.db.compiler.meta.SQLPartitioning;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLPartitioning({
    @SQLPartition(mode = PartitionMode.HASH,  on = "reading_id"),
    @SQLPartition(mode = PartitionMode.RANGE, on = "collected_at", indexOn = true, indexOnUsing = "brin")
})
@SQLIndex(name = "reading_collected_at", columns = { "reading_id", "collected_at" }, using = "btree", since = @SQLVersion({4, 0, 0}))
public abstract class StoredReading implements Serializable
{
    private static final long serialVersionUID = 1L;
      
    @SQLColumn(index = 1, name = "reading_id", since = @SQLVersion({4, 0, 0}))
    protected UUID readingId;
    
    @SQLColumn(index = 2, name = "site_id", since = @SQLVersion({4, 0, 0}))
    protected UUID siteId;
    
    @SQLColumn(index = 3, name = "collected_at", since = @SQLVersion({4, 0, 0}))
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
