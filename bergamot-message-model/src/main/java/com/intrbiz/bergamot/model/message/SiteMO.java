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
    
    @JsonProperty("pool_count")
    protected int poolCount;

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

    public int getPoolCount()
    {
        return poolCount;
    }

    public void setPoolCount(int poolCount)
    {
        this.poolCount = poolCount;
    }
    
    /**
     * Get the site id for the given object id
     * 
     * @param objectId
     * @return
     */
    public static UUID getSiteId(UUID objectId)
    {
        return new UUID((objectId.getMostSignificantBits() & 0xFFFFFFFF_FFFF0000L) | 0x0000000000004000L, 0x80000000_00000000L);
    }

    /**
     * Set the site id into the given object id
     * 
     * @param siteId
     * @param objectId
     * @return
     */
    public static UUID setSiteId(UUID siteId, UUID objectId)
    {
        return new UUID((siteId.getMostSignificantBits() & 0xFFFFFFFF_FFFF0000L) | (objectId.getMostSignificantBits() & 0x00000000_0000FFFFL), objectId.getLeastSignificantBits());
    }
}
