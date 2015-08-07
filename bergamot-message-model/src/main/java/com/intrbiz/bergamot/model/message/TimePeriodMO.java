package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Contact message object
 */
@JsonTypeName("bergamot.time_period")
public class TimePeriodMO extends SecuredObjectMO
{
    @JsonProperty("excludes")
    private List<TimePeriodMO> excludes = new LinkedList<TimePeriodMO>();
    
    @JsonProperty("ranges")
    private List<String> ranges = new LinkedList<String>();
    
    public TimePeriodMO()
    {
        super();
    }

    public List<TimePeriodMO> getExcludes()
    {
        return excludes;
    }

    public void setExcludes(List<TimePeriodMO> excludes)
    {
        this.excludes = excludes;
    }

    public List<String> getRanges()
    {
        return ranges;
    }

    public void setRanges(List<String> ranges)
    {
        this.ranges = ranges;
    }
}
