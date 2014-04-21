package com.intrbiz.bergamot.model.timeperiod;

import java.util.Calendar;

import com.intrbiz.bergamot.model.timeperiod.util.Month;

public class MonthQualifier extends ComposedTimeRange
{
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

    public String toString()
    {
        return this.month.toString().toLowerCase() + " " + super.toString();
    }
}
