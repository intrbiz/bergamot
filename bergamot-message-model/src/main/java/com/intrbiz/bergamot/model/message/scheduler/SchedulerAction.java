package com.intrbiz.bergamot.model.message.scheduler;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;
import com.intrbiz.bergamot.model.message.SiteMO;

/**
 * Alter the scheduling
 */
public abstract class SchedulerAction extends Message
{   
    @JsonProperty("check")
    private UUID check;
    
    public SchedulerAction()
    {
        super();
    }
    
    public SchedulerAction(UUID check)
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
    
    public UUID getSiteId()
    {
        return SiteMO.getSiteId(this.check);
    }
}
