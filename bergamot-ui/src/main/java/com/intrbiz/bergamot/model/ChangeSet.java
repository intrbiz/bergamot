package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ChangeSet implements Serializable
{
    private static final long serialVersionUID = 1L;

    private UUID id;
    
    private String summary;
    
    private String description;
    
    private List<Change> changes = new LinkedList<Change>();
    
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

    public List<Change> getChanges()
    {
        return changes;
    }

    public void setChanges(List<Change> changes)
    {
        this.changes = changes;
    }
}
