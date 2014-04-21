package com.intrbiz.bergamot.model;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.config.model.TimeperiodCfg;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;

/**
 * A calendar of when checks should be executed
 */
public class TimePeriod extends NamedObject implements TimeRange
{
    private Logger logger = Logger.getLogger(TimePeriod.class);

    private Map<UUID, Checkable> checks = new TreeMap<UUID, Checkable>();

    private List<TimePeriod> excludes = new LinkedList<TimePeriod>();

    private List<TimeRange> ranges = new LinkedList<TimeRange>();

    public TimePeriod()
    {
        super();
    }

    public void configure(TimeperiodCfg cfg)
    {
        this.name = cfg.getTimeperiodName();
        this.displayName = Util.coalesce(cfg.resolveAlias(), this.name);
    }

    public Collection<Checkable> getChecks()
    {
        return this.checks.values();
    }

    public Checkable getCheck(UUID id)
    {
        return this.checks.get(id);
    }

    public boolean containsCheck(UUID id)
    {
        return this.checks.containsKey(id);
    }

    public int getCheckCount()
    {
        return this.checks.size();
    }

    public void addCheck(Checkable check)
    {
        this.checks.put(check.getId(), check);
        // TODO differentiate between check and notification periods
        check.setCheckPeriod(this);
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
