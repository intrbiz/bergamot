package com.intrbiz.bergamot.model.timeperiod;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Calendar;

public class DayOfMonthQualifier extends ComposedTimeRange
{
    private static final long serialVersionUID = 1L;
    
    private int dayOfMonth;

    public DayOfMonthQualifier()
    {
        super();
    }

    public DayOfMonthQualifier(int dayOfMonth)
    {
        super();
        this.dayOfMonth = dayOfMonth;
    }

    public DayOfMonthQualifier(int dayOfMonth, TimeRange... ranges)
    {
        super(ranges);
        this.dayOfMonth = dayOfMonth;
    }

    public int getDayOfMonth()
    {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth)
    {
        this.dayOfMonth = dayOfMonth;
    }

    @Override
    public boolean isInTimeRange(Calendar calendar)
    {
        int dom = this.dayOfMonth > 0 ? this.dayOfMonth : (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + this.dayOfMonth + 1);
        return calendar.get(Calendar.DAY_OF_MONTH) == dom && super.isInTimeRange(calendar);
    }
    
    @Override
    public LocalDateTime computeNextStartTime(Clock clock)
    {
        LocalDateTime next = super.computeNextStartTime(clock);
        if (next == null) return null;
        // set the day of month
        int dom = this.dayOfMonth > 0 ? this.dayOfMonth : (next.getMonth().maxLength() + this.dayOfMonth + 1);
        next = next.withDayOfMonth(dom);
        // is it next month
        if (! next.isAfter(LocalDateTime.now(clock))) next = next.plusMonths(1);
        // check the date is valid
        return (next.isAfter(LocalDateTime.now(clock))) ? next : null;
    }

    public String toString()
    {
        return this.dayOfMonth + " " + super.toString();
    }
}
