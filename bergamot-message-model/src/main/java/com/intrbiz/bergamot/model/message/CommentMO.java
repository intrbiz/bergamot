package com.intrbiz.bergamot.model.message;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.comment")
public class CommentMO extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The comment id
     */
    @JsonProperty("id")
    private UUID id;

    /**
     * The content type of the comment: "plain", "markdown", "html"
     */
    @JsonProperty("format")
    protected String format = "plain";

    /**
     * The type of the comment: "general", "acknowledgement", ...
     */
    @JsonProperty("comment_type")
    protected String commentType;

    /**
     * The summary (title) of the comment
     */
    @JsonProperty("summary")
    protected String summary;

    /**
     * The comment
     */
    @JsonProperty("comment")
    protected String comment;

    /**
     * When was it created
     */
    @JsonProperty("created")
    protected long created;

    /**
     * When was it updated
     */
    @JsonProperty("updated")
    protected long updated;

    /**
     * Who created this alert
     */
    @JsonProperty("author")
    private ContactMO author;
    
    public CommentMO()
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

    public ContactMO getAuthor()
    {
        return author;
    }

    public void setAuthor(ContactMO author)
    {
        this.author = author;
    }
}
