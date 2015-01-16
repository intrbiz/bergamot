package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Remove the check from the scheduler
 */
@JsonTypeName("bergamot.unschedule_check")
public class UnscheduleCheck extends ActiveCheckSchedulerAction
{
    public UnscheduleCheck()
    {
        super();
    }
    
    public UnscheduleCheck(UUID check)
    {
        super(check);
    }
}
