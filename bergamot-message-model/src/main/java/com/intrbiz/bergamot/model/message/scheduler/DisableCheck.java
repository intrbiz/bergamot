package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Disable the scheduling of an active check
 */
@JsonTypeName("bergamot.disable_check")
public class DisableCheck extends SchedulerAction
{
    public DisableCheck()
    {
        super();
    }
    
    public DisableCheck(UUID check)
    {
        super(check);
    }
}
