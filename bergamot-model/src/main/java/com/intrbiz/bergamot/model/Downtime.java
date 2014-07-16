package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * Operational Downtime against a check, which is transiently added for 
 * operational purposes.
 * 
 * During downtime a check is suppressed, it will execute, the status will update 
 * however notifications will not be sent and the state will not affect any 
 * dependencies.
 */
@SQLTable(schema = BergamotDB.class, name = "downtime", since = @SQLVersion({ 1, 0, 0 }))
public class Downtime implements Serializable, TimeRange
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The unique ID for this downtime
     */
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    private UUID id;

    @SQLColumn(index = 2, name = "site_id", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT)
    private UUID siteId;

    /**
     * The check to which this downtime applies
     */
    @SQLColumn(index = 3, name = "check_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID checkId;
    
    @SQLColumn(index = 4, name = "summary", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected String summary;

    @SQLColumn(index = 5, name = "description", since = @SQLVersion({ 1, 0, 0 }))
    protected String description;

    /**
     * When this downtime was added
     */
    @SQLColumn(index = 6, name = "created", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp created = new Timestamp(System.currentTimeMillis());

    /**
     * When this downtime was last modified
     */
    @SQLColumn(index = 7, name = "updated", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp updated = new Timestamp(System.currentTimeMillis());
    
    /**
     * Whom created this downtime
     */
    @SQLColumn(index = 8, name = "created_by", since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Contact.class, on = "id", onDelete = Action.SET_NULL, onUpdate = Action.SET_NULL)
    protected UUID createdBy;
    
    /**
     * When does this downtime start (in UTC)
     */
    @SQLColumn(index = 9, name = "starts", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp starts;
    
    /**
     * When does this downtime end (in UTC)
     */
    @SQLColumn(index = 10, name = "ends", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp ends;

    public Downtime()
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

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
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

    public UUID getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy)
    {
        this.createdBy = createdBy;
    }

    public Timestamp getStarts()
    {
        return starts;
    }

    public void setStarts(Timestamp starts)
    {
        this.starts = starts;
    }

    public Timestamp getEnds()
    {
        return ends;
    }

    public void setEnds(Timestamp ends)
    {
        this.ends = ends;
    }

    @Override
    public boolean isInTimeRange(Calendar calendar)
    {
        return calendar.getTimeInMillis() >= this.getStarts().getTime() && calendar.getTimeInMillis() <= this.getEnds().getTime();
    }

    @Override
    public LocalDateTime computeNextStartTime(Clock clock)
    {
        return null;
    }
    
    public String toString()
    {
        return "Downtime { check => " + this.getCheckId() + ", starts => " + this.getStarts() + ", ends => " + this.getEnds() + ", summary => " + this.getSummary()  + " }";
    }
}
