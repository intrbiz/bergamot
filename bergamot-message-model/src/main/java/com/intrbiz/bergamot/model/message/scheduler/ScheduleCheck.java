package com.intrbiz.bergamot.model.message.scheduler;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ActiveCheckMO;

/**
 * Schedule an active check
 */
@JsonTypeName("bergamot.schedule_check")
public class ScheduleCheck extends ActiveCheckSchedulerAction
{
    public ScheduleCheck()
    {
        super();
    }
    
    public ScheduleCheck(ActiveCheckMO check)
    {
        super(check);
    }
}
