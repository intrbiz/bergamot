package com.intrbiz.bergamot.model.timeperiod;

import java.util.Calendar;

import com.intrbiz.bergamot.model.timeperiod.util.DayOfWeek;

public class DayOfWeekQualifier extends ComposedTimeRange
{
    private static final long serialVersionUID = 1L;
    
    private DayOfWeek dayOfWeek;

    public DayOfWeekQualifier()
    {
        super();
    }
    
    public DayOfWeekQualifier(DayOfWeek dayOfWeek)
    {
        super();
        this.dayOfWeek = dayOfWeek;
    }
    
    public DayOfWeekQualifier(DayOfWeek dayOfWeek, TimeRange... ranges)
    {
        super(ranges);
        this.dayOfWeek = dayOfWeek;
    }

    public DayOfWeek getDayOfWeek()
    {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek)
    {
        this.dayOfWeek = dayOfWeek;
    }

    @Override
    public boolean isInTimeRange(Calendar calendar)
    {
        return calendar.get(Calendar.DAY_OF_WEEK) == this.dayOfWeek.getDayOfWeek() && super.isInTimeRange(calendar);
    }

    public String toString()
    {
        return this.dayOfWeek.toString().toLowerCase() + " " + super.toString();
    }
}
