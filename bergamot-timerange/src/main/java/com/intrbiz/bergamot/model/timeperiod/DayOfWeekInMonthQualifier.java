package com.intrbiz.bergamot.model.timeperiod;

import java.util.Calendar;

public class DayOfWeekInMonthQualifier extends ComposedTimeRange
{
    private static final long serialVersionUID = 1L;
    
    private int dayOfWeekInMonth;

    public DayOfWeekInMonthQualifier()
    {
        super();
    }

    public DayOfWeekInMonthQualifier(int dayOfWeekInMonth)
    {
        super();
        this.dayOfWeekInMonth = dayOfWeekInMonth;
    }

    public DayOfWeekInMonthQualifier(int dayOfWeekInMonth, TimeRange... ranges)
    {
        super(ranges);
        this.dayOfWeekInMonth = dayOfWeekInMonth;
    }

    public int getDayOfWeekInMonth()
    {
        return dayOfWeekInMonth;
    }

    public void setDayOfWeekInMonth(int dayOfWeekInMonth)
    {
        this.dayOfWeekInMonth = dayOfWeekInMonth;
    }

    @Override
    public boolean isInTimeRange(Calendar calendar)
    {
        int dowim = this.dayOfWeekInMonth > 0 ? this.dayOfWeekInMonth : (calendar.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH) + this.dayOfWeekInMonth + 1);
        return calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == dowim && super.isInTimeRange(calendar);
    }

    public String toString()
    {
        return this.dayOfWeekInMonth + " " + super.toString();
    }
}
