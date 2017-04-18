package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.sla")
public class SLAMO extends MessageObject
{
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("target")
    private float target;
    
    @JsonProperty("periods")
    private List<SLAPeriodMO> periods = new LinkedList<SLAPeriodMO>();
    
    public SLAMO()
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

    public float getTarget()
    {
        return target;
    }

    public void setTarget(float target)
    {
        this.target = target;
    }

    public List<SLAPeriodMO> getPeriods()
    {
        return periods;
    }

    public void setPeriods(List<SLAPeriodMO> periods)
    {
        this.periods = periods;
    }
}
