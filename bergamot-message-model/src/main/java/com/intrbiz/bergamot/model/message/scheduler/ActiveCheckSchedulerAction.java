package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An action which alters with the scheduling of an active check
 */
public class ActiveCheckSchedulerAction extends SchedulerAction
{
    @JsonProperty("check")
    private UUID check;
    
    public ActiveCheckSchedulerAction()
    {
        super();
    }
    
    public ActiveCheckSchedulerAction(UUID check)
    {
        super();
        this.check = check;
    }

    public UUID getCheck()
    {
        return check;
    }

    public void setCheck(UUID check)
    {
        this.check = check;
    }
}
