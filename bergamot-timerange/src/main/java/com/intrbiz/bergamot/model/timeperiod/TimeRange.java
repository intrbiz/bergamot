package com.intrbiz.bergamot.model.timeperiod;

import java.io.Serializable;
import java.util.Calendar;

public interface TimeRange extends Serializable
{
    boolean isInTimeRange(Calendar calendar);
}
