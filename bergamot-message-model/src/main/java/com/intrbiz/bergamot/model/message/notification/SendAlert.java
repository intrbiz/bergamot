package com.intrbiz.bergamot.model.message.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An alert
 */
@JsonTypeName("bergamot.send_alert")
public class SendAlert extends CheckNotification
{
    /**
     * Is this alert notification an escalation of a previous alert
     */
    @JsonProperty("escalation")
    private boolean escalation = false;
    
    public SendAlert()
    {
        super();
    }
    
    public String getNotificationType()
    {
        return "alert";
    }

    public boolean isEscalation()
    {
        return escalation;
    }

    public void setEscalation(boolean escalation)
    {
        this.escalation = escalation;
    }
}
