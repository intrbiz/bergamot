package com.intrbiz.bergamot.model.message;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.sla.fixed-period")
public class SLAFixedPeriodMO extends SLAPeriodMO
{   
    @JsonProperty("start")
    private Date start;
    
    @JsonProperty("end")
    private Date end;
    
    public SLAFixedPeriodMO()
    {
        super();
    }

    public Date getStart()
    {
        return start;
    }

    public void setStart(Date start)
    {
        this.start = start;
    }

    public Date getEnd()
    {
        return end;
    }

    public void setEnd(Date end)
    {
        this.end = end;
    }
}
