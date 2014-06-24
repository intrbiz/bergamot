package com.intrbiz.bergamot.model.timeperiod;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Calendar;

import com.intrbiz.bergamot.model.timeperiod.util.DayOfWeek;
import com.intrbiz.bergamot.model.timeperiod.util.Month;

public class DayOfWeekInMonthQualifier extends MonthQualifier
{
    private static final long serialVersionUID = 1L;

    private int dayOfWeekInMonth;

    private DayOfWeek dayOfWeek;

    public DayOfWeekInMonthQualifier()
    {
        super();
    }

    public DayOfWeekInMonthQualifier(Month month, DayOfWeek dayOfWeek, int dayOfWeekInMonth)
    {
        super(month);
        this.dayOfWeek = dayOfWeek;
        this.dayOfWeekInMonth = dayOfWeekInMonth;
    }

    public DayOfWeekInMonthQualifier(Month month, DayOfWeek dayOfWeek, int dayOfWeekInMonth, TimeRange... ranges)
    {
        super(month, ranges);
        this.dayOfWeek = dayOfWeek;
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
        // TODO: we need better tests for this
        int dowim = this.dayOfWeekInMonth > 0 ? this.dayOfWeekInMonth : (calendar.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH) + this.dayOfWeekInMonth + 1);
        return calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == dowim && super.isInTimeRange(calendar);
    }

    @Override
    public LocalDateTime computeNextStartTime(Clock clock)
    {
        LocalDateTime next = super.computeNextStartTime(clock);
        if (next == null) return null;
        // compute the first x day of the week in the month
        if (this.dayOfWeekInMonth > 0)
        {
            next = next.withDayOfMonth(1);
            int addDays = this.dayOfWeek.toJavaTime().getValue() - next.getDayOfWeek().getValue();
            if (addDays < 0) addDays = addDays + 7;
            if (addDays > 0) next = next.plusDays(addDays);
            // increment the weeks
            for (int i = (this.dayOfWeekInMonth - 1); i > 0 ; i--)
            {
                next = next.plusDays(7);
            }
        }
        else
        {
            next = next.withDayOfMonth(next.getMonth().maxLength());
            int subDays = next.getDayOfWeek().getValue() - this.dayOfWeek.toJavaTime().getValue();
            if (subDays < 0) subDays = subDays + 7;
            if (subDays > 0) next = next.minusDays(subDays);
            // decrement the weeks
            for (int i = (this.dayOfWeekInMonth + 1); i < 0; i++)
            {
                next = next.minusDays(7);
            }
        }
        // check the date is valid
        return (next.isAfter(LocalDateTime.now(clock))) ? next : null;
    }

    public String toString()
    {
        return this.dayOfWeek.toString().toLowerCase() + " " + this.dayOfWeekInMonth + " " + super.toString();
    }
}
