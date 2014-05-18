package com.intrbiz.bergamot.model.timeperiod;


public class DayQualifier extends ComposedTimeRange
{
    public DayQualifier()
    {
        super();
    }

    public DayQualifier(TimeRange... ranges)
    {
        super(ranges);
    }

    public String toString()
    {
        return "day " + super.toString();
    }
}
