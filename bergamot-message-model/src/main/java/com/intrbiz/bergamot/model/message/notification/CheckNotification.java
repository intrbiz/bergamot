package com.intrbiz.bergamot.model.message.notification;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.CheckMO;

/**
 * A notification about a check
 */
public abstract class CheckNotification extends Notification
{
    @JsonProperty("check")
    private CheckMO check;
    
    @JsonProperty("alert_id")
    private UUID alertId;
    
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

    public UUID getAlertId()
    {
        return alertId;
    }

    public void setAlertId(UUID alertId)
    {
        this.alertId = alertId;
    }
}
