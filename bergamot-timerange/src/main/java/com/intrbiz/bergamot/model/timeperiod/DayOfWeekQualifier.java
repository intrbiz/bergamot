package com.intrbiz.bergamot.model.timeperiod;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
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
    
    @Override
    public LocalDateTime computeNextStartTime(Clock clock)
    {
        LocalDateTime next = super.computeNextStartTime(clock);
        if (next == null) return null;
        next = next.with(ChronoField.DAY_OF_WEEK, this.dayOfWeek.toJavaTime().getValue());
        // is it next week?
        if (! next.isAfter(LocalDateTime.now(clock))) next = next.plusDays(7);
        // check the date is valid
        return (next.isAfter(LocalDateTime.now(clock))) ? next : null;
    }

    public String toString()
    {
        return this.dayOfWeek.toString().toLowerCase() + " " + super.toString();
    }
}
