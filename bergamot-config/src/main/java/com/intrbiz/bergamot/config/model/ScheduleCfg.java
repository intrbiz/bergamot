package com.intrbiz.bergamot.config.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.Coalesce;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyString;
import com.intrbiz.bergamot.util.TimeInterval;

@XmlType(name = "schedule")
@XmlRootElement(name = "schedule")
public class ScheduleCfg implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private String every;

    private String retryEvery;
    
    private String changingEvery;
    
    private String timePeriod;

    public ScheduleCfg()
    {
        super();
    }

    /**
     * The interval to use (in minutes) when the check is ok.
     * @return
     */
    @XmlAttribute(name = "every")
    @ResolveWith(Coalesce.class)
    public String getEvery()
    {
        return every;
    }

    public void setEvery(String every)
    {
        this.every = every;
    }
    
    public TimeInterval getEveryTimeInterval()
    {
        return this.every == null ? null : TimeInterval.fromString(this.every);
    }
    
    public TimeInterval getEveryTimeInterval(TimeInterval defaultValue)
    {
        return Util.coalesce(this.getEveryTimeInterval(), defaultValue);
    }

    /**
     * The interval to use (in minutes) when the check is not ok.
     */
    @XmlAttribute(name = "retry-every")
    @ResolveWith(Coalesce.class)
    public String getRetryEvery()
    {
        return retryEvery;
    }

    public void setRetryEvery(String retryEvery)
    {
        this.retryEvery = retryEvery;
    }
    
    public TimeInterval getRetryEveryTimeInterval()
    {
        return this.retryEvery == null ? null : TimeInterval.fromString(this.retryEvery);
    }
    
    public TimeInterval getRetryEveryTimeInterval(TimeInterval defaultValue)
    {
        return Util.coalesce(this.getRetryEveryTimeInterval(), defaultValue);
    }

    /**
     * The interval to use (in minutes) when the check is transitioning (chaning) 
     * state.  If this is not specified then the retry interval is used.
     */
    @XmlAttribute(name = "changing-every")
    @ResolveWith(Coalesce.class)
    public String getChangingEvery()
    {
        return changingEvery;
    }

    public void setChangingEvery(String changingEvery)
    {
        this.changingEvery = changingEvery;
    }
    
    public TimeInterval getChangingEveryTimeInterval()
    {
        return this.changingEvery == null ? null : TimeInterval.fromString(this.changingEvery);
    }
    
    public TimeInterval getChangingEveryTimeInterval(TimeInterval defaultValue)
    {
        return Util.coalesce(this.getChangingEveryTimeInterval(), defaultValue);
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
