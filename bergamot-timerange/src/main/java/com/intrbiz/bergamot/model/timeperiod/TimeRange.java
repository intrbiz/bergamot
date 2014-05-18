package com.intrbiz.bergamot.model.timeperiod;

import java.util.Calendar;

public interface TimeRange
{
    boolean isInTimeRange(Calendar calendar);
}
