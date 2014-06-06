package com.intrbiz.bergamot.model.message.scheduler;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ActiveCheckMO;

/**
 * Enable the scheduling of an active check
 */
@JsonTypeName("bergamot.enable_check")
public class EnableCheck extends ActiveCheckSchedulerAction
{
    public EnableCheck()
    {
        super();
    }
    
    public EnableCheck(ActiveCheckMO check)
    {
        super(check);
    }
}
