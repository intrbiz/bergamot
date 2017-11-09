package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.sla.rolling-period")
public class SLARollingPeriodMO extends SLAPeriodMO
{   
    @JsonProperty("granularity")
    private String granularity;
    
    public SLARollingPeriodMO()
    {
        super();
    }

    public String getGranularity()
    {
        return granularity;
    }

    public void setGranularity(String granularity)
    {
        this.granularity = granularity;
    }
}
