package com.intrbiz.bergamot.model.message.notification;

import java.util.UUID;

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
    
    /**
     * The alert duration of the escalation policy
     */
    @JsonProperty("escalated_after")
    private long escalatedAfter = 0;
    
    @JsonProperty("escalation_id")
    private UUID escalationId;
    
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

    public long getEscalatedAfter()
    {
        return escalatedAfter;
    }

    public void setEscalatedAfter(long escalatedAfter)
    {
        this.escalatedAfter = escalatedAfter;
    }

    public UUID getEscalationId()
    {
        return escalationId;
    }

    public void setEscalationId(UUID escalationId)
    {
        this.escalationId = escalationId;
    }
}
