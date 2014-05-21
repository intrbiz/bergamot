package com.intrbiz.bergamot.model.message;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class NamedObjectMO extends MessageObject
{
    @JsonProperty("id")
    protected UUID id;

    @JsonProperty("name")
    protected String name;
 
    @JsonProperty("summary")
    protected String summary;
    
    @JsonProperty("description")
    protected String description;
    
    public NamedObjectMO()
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
}
