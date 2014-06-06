package com.intrbiz.bergamot.model.message.notification;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.CheckMO;
import com.intrbiz.bergamot.model.message.ContactMO;
import com.intrbiz.bergamot.model.message.Message;

/**
 * A notification
 */
public abstract class Notification extends Message
{
    @JsonProperty("raised")
    private long raised;

    @JsonProperty("to")
    private List<ContactMO> to = new LinkedList<ContactMO>();

    @JsonProperty("check")
    private CheckMO check;
    
    @JsonProperty("alert_id")
    private UUID alertId;

    public Notification()
    {
        super();
    }
    
    @JsonIgnore
    public abstract String getNotificationType();

    public long getRaised()
    {
        return raised;
    }

    public void setRaised(long raised)
    {
        this.raised = raised;
    }

    public List<ContactMO> getTo()
    {
        return to;
    }

    public void setTo(List<ContactMO> to)
    {
        this.to = to;
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
