package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * A notification about a check
 */
public abstract class CheckNotification extends Notification
{
    @JsonProperty("check")
    private CheckMO check;
    
    public CheckNotification()
    {
        super();
    }

    public CheckMO getCheck()
    {
        return check;
    }

    public void setCheck(CheckMO check)
    {
        this.check = check;
    }
}
