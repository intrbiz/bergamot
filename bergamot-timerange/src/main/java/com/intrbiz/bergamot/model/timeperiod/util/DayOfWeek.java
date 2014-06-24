package com.intrbiz.bergamot.model.timeperiod.util;

import java.util.Calendar;

public enum DayOfWeek
{
    SUNDAY    (Calendar.SUNDAY, java.time.DayOfWeek.SUNDAY),
    MONDAY    (Calendar.MONDAY, java.time.DayOfWeek.MONDAY),
    TUESDAY   (Calendar.TUESDAY, java.time.DayOfWeek.TUESDAY),
    WEDNESDAY (Calendar.WEDNESDAY, java.time.DayOfWeek.WEDNESDAY),
    THURSDAY  (Calendar.THURSDAY, java.time.DayOfWeek.THURSDAY),
    FRIDAY    (Calendar.FRIDAY, java.time.DayOfWeek.FRIDAY),
    SATURDAY  (Calendar.SATURDAY, java.time.DayOfWeek.SATURDAY);
    
    private final int dow;
    private final java.time.DayOfWeek javaTime;
    
    private DayOfWeek(int dow, java.time.DayOfWeek javaTime)
    {
        this.dow = dow;
        this.javaTime = javaTime;
    }
    
    public int getDayOfWeek()
    {
        return dow;
    }
    
    public java.time.DayOfWeek toJavaTime()
    {
        return this.javaTime;
    }
}
