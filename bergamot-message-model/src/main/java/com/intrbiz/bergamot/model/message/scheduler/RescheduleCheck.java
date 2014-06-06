package com.intrbiz.bergamot.model.message.scheduler;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ActiveCheckMO;

/**
 * Update the scheduling of an active check
 */
@JsonTypeName("bergamot.reschedule_check")
public class RescheduleCheck extends ActiveCheckSchedulerAction
{
    public RescheduleCheck()
    {
        super();
    }
    
    public RescheduleCheck(ActiveCheckMO check)
    {
        super(check);
    }
}
