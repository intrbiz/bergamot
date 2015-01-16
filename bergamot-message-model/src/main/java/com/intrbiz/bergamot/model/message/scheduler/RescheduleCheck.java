package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;

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
    
    public RescheduleCheck(UUID check)
    {
        super(check);
    }
}
