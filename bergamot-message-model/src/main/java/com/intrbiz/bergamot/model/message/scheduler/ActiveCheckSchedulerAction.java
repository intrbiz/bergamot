package com.intrbiz.bergamot.model.message.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.ActiveCheckMO;

/**
 * An action which alters with the scheduling of an active check
 */
public class ActiveCheckSchedulerAction extends SchedulerAction
{
    @JsonProperty("check")
    private ActiveCheckMO check;
    
    public ActiveCheckSchedulerAction()
    {
        super();
    }
    
    public ActiveCheckSchedulerAction(ActiveCheckMO check)
    {
        super();
        this.check = check;
    }

    public ActiveCheckMO getCheck()
    {
        return check;
    }

    public void setCheck(ActiveCheckMO check)
    {
        this.check = check;
    }
}
