package com.intrbiz.bergamot.model.timeperiod.util;

import java.util.Calendar;

public enum DayOfWeek
{
    SUNDAY    (Calendar.SUNDAY),
    MONDAY    (Calendar.MONDAY),
    TUESDAY   (Calendar.TUESDAY),
    WEDNESDAY (Calendar.WEDNESDAY),
    THURSDAY  (Calendar.THURSDAY),
    FRIDAY    (Calendar.FRIDAY),
    SATURDAY  (Calendar.SATURDAY);
    
    private final int dow;
    
    private DayOfWeek(int dow)
    {
        this.dow = dow;
    }
    
    public int getDayOfWeek()
    {
        return dow;
    }
}
