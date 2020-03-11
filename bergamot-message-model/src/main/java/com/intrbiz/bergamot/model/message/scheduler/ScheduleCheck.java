package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Schedule an active check
 */
@JsonTypeName("bergamot.schedule_check")
public class ScheduleCheck extends SchedulerAction
{
    public ScheduleCheck()
    {
        super();
    }
    
    public ScheduleCheck(UUID check)
    {
        super(check);
    }
}
