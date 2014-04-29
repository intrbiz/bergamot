package com.intrbiz.bergamot.model.message;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class NamedObjectMO extends MessageObject
{
    @JsonProperty("id")
    protected UUID id;

    @JsonProperty("name")
    protected String name;
 
    @JsonProperty("display_name")
    protected String displayName;
    
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

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
}
