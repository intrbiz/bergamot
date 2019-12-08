package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Enable the scheduling of an active check
 */
@JsonTypeName("bergamot.enable_check")
public class EnableCheck extends SchedulerAction
{
    public EnableCheck()
    {
        super();
    }
    
    public EnableCheck(UUID check)
    {
        super(check);
    }
}
