package com.intrbiz.bergamot.model.timeperiod;

import java.util.Calendar;

public final class HourRange implements TimeRange
{
    private static final long serialVersionUID = 1L;
    
    private final int startHour;

    private final int startMinute;

    private final int startSecond;

    private final int stopHour;

    private final int stopMinute;

    private final int stopSecond;

    public HourRange(int startHour, int startMinute, int startSecond, int stopHour, int stopMinute, int stopSecond)
    {
        super();
        this.startHour   = startHour;
        this.startMinute = startMinute;
        this.startSecond = startSecond;
        this.stopHour    = stopHour;
        this.stopMinute  = stopMinute;
        this.stopSecond  = stopSecond;
    }
    
    public HourRange(int startHour, int startMinute, int stopHour, int stopMinute)
    {
        this(startHour, startMinute, 0, stopHour, stopMinute, 0);
    }

    public int getStartHour()
    {
        return startHour;
    }

    public int getStartMinute()
    {
        return startMinute;
    }

    public int getStartSecond()
    {
        return startSecond;
    }

    public int getStopHour()
    {
        return stopHour;
    }

    public int getStopMinute()
    {
        return stopMinute;
    }

    public int getStopSecond()
    {
        return stopSecond;
    }

    public boolean isInTimeRange(Calendar calendar)
    {
        long sod   = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + calendar.get(Calendar.SECOND);
        long start = (this.startHour * 3600) + (this.startMinute * 60) + this.startSecond;
        long stop  = (this.stopHour * 3600) + (this.stopMinute * 60) + this.stopSecond;
        return start <= sod && sod < stop;
    }
    
    public String toString()
    {
        return (this.startHour < 10 ? "0" : "") + this.startHour + ":" + 
               (this.startMinute < 10 ? "0" : "") + this.startMinute + "-" + 
               (this.stopHour < 10 ? "0" : "") + this.stopHour + ":" + 
               (this.stopMinute < 10 ? "0" : "") + this.stopMinute;
    }
}
