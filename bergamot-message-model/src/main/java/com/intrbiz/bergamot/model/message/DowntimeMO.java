package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("bergamot.downtime")
public class DowntimeMO extends MessageObject implements CommentedMO
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The unique ID for this downtime
     */
    @JsonProperty("id")
    protected UUID id;

    /**
     * The check to which this downtime applies
     */
    @JsonProperty("check")
    protected CheckMO check;
    
    /**
     * Summary of this downtime
     */
    @JsonProperty("summary")
    protected String summary;

    /**
     * Description of this downtime
     */
    @JsonProperty("description")
    protected String description;

    /**
     * When this downtime was added
     */
    @JsonProperty("created")
    protected long created;

    /**
     * When this downtime was last modified
     */
    @JsonProperty("updated")
    protected long updated;
    
    /**
     * Whom created this downtime
     */
    @JsonProperty("created_by")
    protected ContactMO createdBy;
    
    /**
     * When does this downtime start (in UTC)
     */
    @JsonProperty("starts")
    protected long starts;
    
    /**
     * When does this downtime end (in UTC)
     */
    @JsonProperty("ends")
    protected long ends;
    
    /**
     * Comments against this downtime
     */
    @JsonProperty("comments")
    protected List<CommentMO> comments = new LinkedList<CommentMO>();
    
    public DowntimeMO()
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

    public CheckMO getCheck()
    {
        return check;
    }

    public void setCheck(CheckMO check)
    {
        this.check = check;
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

    public long getCreated()
    {
        return created;
    }

    public void setCreated(long created)
    {
        this.created = created;
    }

    public long getUpdated()
    {
        return updated;
    }

    public void setUpdated(long updated)
    {
        this.updated = updated;
    }

    public ContactMO getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(ContactMO createdBy)
    {
        this.createdBy = createdBy;
    }

    public long getStarts()
    {
        return starts;
    }

    public void setStarts(long starts)
    {
        this.starts = starts;
    }

    public long getEnds()
    {
        return ends;
    }

    public void setEnds(long ends)
    {
        this.ends = ends;
    }

    @Override
    public List<CommentMO> getComments()
    {
        return comments;
    }

    @Override
    public void setComments(List<CommentMO> comments)
    {
        this.comments = comments;
    }
}
