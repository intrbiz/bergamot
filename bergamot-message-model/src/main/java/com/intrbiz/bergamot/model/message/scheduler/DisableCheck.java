package com.intrbiz.bergamot.model.message.scheduler;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.ActiveCheckMO;

/**
 * Disable the scheduling of an active check
 */
@JsonTypeName("bergamot.disable_check")
public class DisableCheck extends ActiveCheckSchedulerAction
{
    public DisableCheck()
    {
        super();
    }
    
    public DisableCheck(ActiveCheckMO check)
    {
        super(check);
    }
}
