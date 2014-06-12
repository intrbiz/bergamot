package com.intrbiz.bergamot.model.timeperiod;

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

    public String toString()
    {
        return this.dayOfMonth + " " + super.toString();
    }
}
