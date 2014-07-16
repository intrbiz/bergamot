package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A comment against an alert, check, etc
 */
@SQLTable(schema = BergamotDB.class, name = "comment", since = @SQLVersion({ 1, 0, 0 }))
public class Comment implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * The unique ID for this comment
     */
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    private UUID id;

    @SQLColumn(index = 2, name = "site_id", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT)
    private UUID siteId;

    /**
     * The object against which this comment relates, this could be: 1. a check 2. an alert 3. a downtime
     */
    @SQLColumn(index = 3, name = "object_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID objectId;

    /**
     * The content type of the comment: "plain", "markdown", "html"
     */
    @SQLColumn(index = 4, name = "format", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected String format = "plain";

    /**
     * The type of the comment: "general", "acknowledgement", ...
     */
    @SQLColumn(index = 5, name = "comment_type", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected String commentType = "general";

    /**
     * The summary (title) of the comment
     */
    @SQLColumn(index = 6, name = "summary", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    protected String summary;

    /**
     * The comment
     */
    @SQLColumn(index = 7, name = "comment", since = @SQLVersion({ 1, 0, 0 }))
    protected String comment;

    /**
     * When was it created
     */
    @SQLColumn(index = 8, name = "created", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp created = new Timestamp(System.currentTimeMillis());

    /**
     * When was it updated
     */
    @SQLColumn(index = 9, name = "updated", since = @SQLVersion({ 1, 0, 0 }))
    protected Timestamp updated = new Timestamp(System.currentTimeMillis());

    /**
     * Who created this alert
     */
    @SQLColumn(index = 10, name = "author", since = @SQLVersion({ 1, 0, 0 }))
    private UUID author;

    public Comment()
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

    public UUID getObjectId()
    {
        return objectId;
    }

    public void setObjectId(UUID objectId)
    {
        this.objectId = objectId;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public String getCommentType()
    {
        return commentType;
    }

    public void setCommentType(String commentType)
    {
        this.commentType = commentType;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
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

    public UUID getAuthor()
    {
        return author;
    }

    public void setAuthor(UUID author)
    {
        this.author = author;
    }

    public String toString()
    {
        return "Comment { id => " + this.getId() + ", object => " + this.getObjectId() + ", summary => " + this.getSummary() + " }";
    }
}
