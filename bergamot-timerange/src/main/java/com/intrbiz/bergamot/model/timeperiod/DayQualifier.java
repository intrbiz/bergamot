package com.intrbiz.bergamot.model.timeperiod;


public class DayQualifier extends ComposedTimeRange
{
    private static final long serialVersionUID = 1L;
    
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
