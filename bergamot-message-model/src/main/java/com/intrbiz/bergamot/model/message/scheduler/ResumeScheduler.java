package com.intrbiz.bergamot.model.message.scheduler;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Resume all scheduling operations
 */
@JsonTypeName("bergamot.resume_scheduler")
public class ResumeScheduler extends SchedulerAction
{
    public ResumeScheduler()
    {
        super();
    }
}
