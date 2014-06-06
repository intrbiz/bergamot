package com.intrbiz.bergamot.model.message.scheduler;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Pause all scheduler operations
 */
@JsonTypeName("bergamot.pause_scheduler")
public class PauseScheduler extends SchedulerAction
{
    public PauseScheduler()
    {
        super();
    }
}
