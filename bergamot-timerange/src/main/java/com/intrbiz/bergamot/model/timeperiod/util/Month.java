package com.intrbiz.bergamot.model.timeperiod.util;

import java.util.Calendar;

public enum Month {
    
    JANUARY(Calendar.JANUARY),
    FEBRUARY(Calendar.FEBRUARY),
    MARCH(Calendar.MARCH),
    APRIL(Calendar.APRIL),
    MAY(Calendar.MAY),
    JUNE(Calendar.JUNE),
    JULY(Calendar.JULY),
    AUGUST(Calendar.AUGUST),
    SEPTEMBER(Calendar.SEPTEMBER),
    OCTOBER(Calendar.OCTOBER),
    NOVEMBER(Calendar.NOVEMBER),
    DECEMBER(Calendar.DECEMBER);
    
    private final int month;
    
    private Month(int month)
    {
        this.month = month;
    }
    
    public int getMonth()
    {
        return this.month;
    }
    
    /**
     * 0 indexed
     */
    public static Month valueOf(int ordinal)
    {
        for (Month month : Month.values())
        {
            if (month.ordinal() == ordinal) return month;
        }
        return null;
    }
}
