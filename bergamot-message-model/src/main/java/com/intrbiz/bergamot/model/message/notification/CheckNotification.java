package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * A notification about a check
 */
public abstract class CheckNotification extends Notification
{
    private static final long serialVersionUID = 1L;
    
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
