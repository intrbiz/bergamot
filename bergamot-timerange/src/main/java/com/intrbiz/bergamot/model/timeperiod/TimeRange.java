package com.intrbiz.bergamot.model.timeperiod;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Calendar;

public interface TimeRange extends Serializable
{
    boolean isInTimeRange(Calendar calendar);
    
    LocalDateTime computeNextStartTime(Clock clock);
}
