package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.site")
public class SiteMO extends MessageObject
{
    @JsonProperty("id")
    protected UUID id;

    @JsonProperty("name")
    protected String name;

    @JsonProperty("summary")
    protected String summary;

    @JsonProperty("description")
    protected String description;
    
    @JsonProperty("aliases")
    protected List<String> aliases = new LinkedList<String>();

    public SiteMO()
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

    public List<String> getAliases()
    {
        return aliases;
    }

    public void setAliases(List<String> aliases)
    {
        this.aliases = aliases;
    }
}
