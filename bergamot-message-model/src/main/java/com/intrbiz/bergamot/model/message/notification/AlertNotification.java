package com.intrbiz.bergamot.model.message.notification;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A notification about a alerted check
 */
public abstract class AlertNotification extends CheckNotification
{   
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("alert_id")
    private UUID alertId;
    
    public AlertNotification()
    {
        super();
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
