package com.intrbiz.bergamot.model.timeperiod;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.Util;

public class ComposedTimeRange implements TimeRange
{
    private List<TimeRange> ranges = new LinkedList<TimeRange>();

    public ComposedTimeRange()
    {
        super();
    }

    public ComposedTimeRange(TimeRange... ranges)
    {
        super();
        for (TimeRange range : ranges)
        {
            this.ranges.add(range);
        }
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

    public boolean isInTimeRange(Calendar calendar)
    {
        if (this.ranges.isEmpty()) return true;
        for (TimeRange range : this.ranges)
        {
            if (range.isInTimeRange(calendar)) return true;
        }
        return false;
    }

    public String toString()
    {
        return Util.join(", ", this.ranges);
    }
}
