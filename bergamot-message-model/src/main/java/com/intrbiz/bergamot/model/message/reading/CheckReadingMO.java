package com.intrbiz.bergamot.model.message.reading;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

@JsonTypeName("bergamot.reading.check_reading")
public class CheckReadingMO extends MessageObject
{
    @JsonProperty("reading_id")
    protected UUID readingId;
    
    @JsonProperty("site_id")
    protected UUID siteId;
    
    @JsonProperty("check_id")
    protected UUID checkId;

    @JsonProperty("name")
    protected String name;

    @JsonProperty("summary")
    protected String summary;

    @JsonProperty("description")
    protected String description;
    
    @JsonProperty("unit")
    protected String unit;
    
    @JsonProperty("reading_type")
    protected String readingType;
    
    @JsonProperty("created")
    protected long created;

    @JsonProperty("updated")
    protected long updated;
    
    @JsonProperty("poll_interval")
    protected long pollInterval;
    
    public CheckReadingMO()
    {
        super();
    }

    public CheckReadingMO(UUID readingId, UUID siteId, UUID checkId, String name, String summary, String description, String unit, String readingType, long created, long updated, long pollInterval)
    {
        super();
        this.readingId = readingId;
        this.siteId = siteId;
        this.checkId = checkId;
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.unit = unit;
        this.readingType = readingType;
        this.created = created;
        this.updated = updated;
        this.pollInterval = pollInterval;
    }

    public UUID getReadingId()
    {
        return readingId;
    }

    public void setReadingId(UUID readingId)
    {
        this.readingId = readingId;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
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

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getReadingType()
    {
        return readingType;
    }

    public void setReadingType(String readingType)
    {
        this.readingType = readingType;
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

    public long getPollInterval()
    {
        return pollInterval;
    }

    public void setPollInterval(long pollInterval)
    {
        this.pollInterval = pollInterval;
    }
}
