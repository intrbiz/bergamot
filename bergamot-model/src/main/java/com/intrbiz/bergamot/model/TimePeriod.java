package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.configuration.Configurable;

/**
 * A calendar of when checks should be executed
 */
public class TimePeriod extends NamedObject implements TimeRange, Configurable<TimePeriodCfg>
{
    private Logger logger = Logger.getLogger(TimePeriod.class);

    private TimePeriodCfg config;

    private List<TimePeriod> excludes = new LinkedList<TimePeriod>();

    private List<TimeRange> ranges = new LinkedList<TimeRange>();

    public TimePeriod()
    {
        super();
    }

    @Override
    public TimePeriodCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public void configure(TimePeriodCfg cfg)
    {
        this.config = cfg;
        TimePeriodCfg rcfg = cfg.resolve();
        this.name = rcfg.getName();
        this.displayName = Util.coalesce(rcfg.getSummary(), this.name);
        // load the time ranges
        this.ranges.clear();
        this.ranges.addAll(rcfg.getTimeRanges());
    }

    public List<TimeRange> getRanges()
    {
        return ranges;
    }

    public void addRange(TimeRange range)
    {
        this.ranges.add(range);
    }

    public void setRanges(List<TimeRange> ranges)
    {
        this.ranges = ranges;
    }

    public List<TimePeriod> getExcludes()
    {
        return excludes;
    }

    public void addExclude(TimePeriod exclude)
    {
        this.excludes.add(exclude);
    }

    public void setExcludes(List<TimePeriod> excludes)
    {
        this.excludes = excludes;
    }

    /**
     * Compute whether the given calendar is within this timeperiod.
     */
    @Override
    public boolean isInTimeRange(Calendar calendar)
    {
        logger.trace("Checking if " + calendar.getTime() + " is valid for this time period");
        // check the possbile excludes
        for (TimePeriod exclude : this.excludes)
        {
            if (exclude.isInTimeRange(calendar)) return false;
        }
        // check the ranges
        if (this.ranges.isEmpty()) return true;
        for (TimeRange range : this.ranges)
        {
            if (range.isInTimeRange(calendar)) return true;
        }
        return false;
    }

    public String toString()
    {
        return "TimePeriod " + this.getName();
    }
}
