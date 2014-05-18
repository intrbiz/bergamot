package com.intrbiz.bergamot.config.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.intrbiz.bergamot.config.adapter.CSVAdapter;
import com.intrbiz.bergamot.config.adapter.TimeRangeAdapter;
import com.intrbiz.bergamot.config.resolver.ResolveWith;
import com.intrbiz.bergamot.config.resolver.stratergy.CoalesceEmptyCollection;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;

@XmlType(name = "time-period")
@XmlRootElement(name = "time-period")
public class TimePeriodCfg extends NamedObjectCfg<TimePeriodCfg>
{
    private Set<String> excludes = new LinkedHashSet<String>();

    private List<TimeRange> timeRanges = new LinkedList<TimeRange>();

    public TimePeriodCfg()
    {
        super();
    }

    @XmlJavaTypeAdapter(CSVAdapter.class)
    @XmlAttribute(name = "excludes")
    @ResolveWith(CoalesceEmptyCollection.class)
    public Set<String> getExcludes()
    {
        return excludes;
    }

    public void setExcludes(Set<String> excludes)
    {
        this.excludes = excludes;
    }

    @XmlJavaTypeAdapter(TimeRangeAdapter.class)
    @XmlElement(name = "time-range")
    @ResolveWith(CoalesceEmptyCollection.class)
    public List<TimeRange> getTimeRanges()
    {
        return timeRanges;
    }

    public void setTimeRanges(List<TimeRange> timeRanges)
    {
        this.timeRanges = timeRanges;
    }

    public List<TemplatedObjectCfg<?>> getTemplatedChildObjects()
    {
        List<TemplatedObjectCfg<?>> r = new LinkedList<TemplatedObjectCfg<?>>();
        return r;
    }
}
