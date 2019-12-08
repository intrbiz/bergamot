package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Update the scheduling of an active check
 */
@JsonTypeName("bergamot.reschedule_check")
public class RescheduleCheck extends SchedulerAction
{
    /**
     * The interval for the check to be rescheduled.
     * Optional, if <= 0 it will be ignored.
     */
    @JsonProperty("interval")
    private long interval;
    
    public RescheduleCheck()
    {
        super();
    }
    
    public RescheduleCheck(UUID check)
    {
        super(check);
        this.interval = -1;
    }
    
    public RescheduleCheck(UUID check, long interval)
    {
        super(check);
        this.interval = interval;
    }

    public long getInterval()
    {
        return interval;
    }

    public void setInterval(long interval)
    {
        this.interval = interval;
    }
}
