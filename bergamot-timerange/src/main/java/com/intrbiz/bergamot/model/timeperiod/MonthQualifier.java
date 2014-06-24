package com.intrbiz.bergamot.model.timeperiod;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Calendar;

import com.intrbiz.bergamot.model.timeperiod.util.Month;

public class MonthQualifier extends ComposedTimeRange
{
    private static final long serialVersionUID = 1L;
    
    private Month month;

    public MonthQualifier()
    {
        super();
    }

    public MonthQualifier(Month month)
    {
        super();
        this.month = month;
    }

    public MonthQualifier(Month month, TimeRange... ranges)
    {
        super(ranges);
        this.month = month;
    }

    public Month getMonth()
    {
        return month;
    }

    public void setMonth(Month month)
    {
        this.month = month;
    }

    @Override
    public boolean isInTimeRange(Calendar calendar)
    {
        return calendar.get(Calendar.MONTH) == this.month.getMonth() && super.isInTimeRange(calendar);
    }
    
    @Override
    public LocalDateTime computeNextStartTime(Clock clock)
    {
        LocalDateTime next = super.computeNextStartTime(clock);
        if (next == null) return null;
        next = next.withMonth(this.month.getMonth() + 1);
        // is it next year?
        if (! next.isAfter(LocalDateTime.now(clock))) next = next.plusYears(1);
        // check the date is valid
        return (next.isAfter(LocalDateTime.now(clock))) ? next : null;
    }

    public String toString()
    {
        return this.month.toString().toLowerCase() + " " + super.toString();
    }
}
