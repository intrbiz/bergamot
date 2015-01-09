package com.intrbiz.bergamot.config.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;

@XmlType(name = "schedule")
@XmlRootElement(name = "schedule")
public class ScheduleCfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private Long every;

    private Long retryEvery;
    
    private Long changingEvery;
    
    private String timePeriod;

    public ScheduleCfg()
    {
        super();
    }

    @XmlAttribute(name = "every")
    @ResolveWith(Coalesce.class)
    public Long getEvery()
    {
        return every;
    }

    public void setEvery(Long every)
    {
        this.every = every;
    }

    @XmlAttribute(name = "retry-every")
    @ResolveWith(Coalesce.class)
    public Long getRetryEvery()
    {
        return retryEvery;
    }

    public void setRetryEvery(Long retryEvery)
    {
        this.retryEvery = retryEvery;
    }

    @XmlAttribute(name = "changing-every")
    @ResolveWith(Coalesce.class)
    public Long getChangingEvery()
    {
        return changingEvery;
    }

    public void setChangingEvery(Long changingEvery)
    {
        this.changingEvery = changingEvery;
    }

    @XmlAttribute(name="time-period")
    @ResolveWith(CoalesceEmptyString.class)
    public String getTimePeriod()
    {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod)
    {
        this.timePeriod = timePeriod;
    }
}
