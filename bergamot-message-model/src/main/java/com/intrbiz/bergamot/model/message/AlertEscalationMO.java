package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.alert-escalation")
public class AlertEscalationMO extends MessageObject
{
    @JsonProperty("escalation_id")
    private UUID escalationId;

    @JsonProperty("after")
    private long after;

    @JsonProperty("escalated_at")
    private long escalatedAt;

    @JsonProperty("notified")
    private List<ContactMO> notified = new LinkedList<ContactMO>();

    public AlertEscalationMO()
    {
        super();
    }

    public UUID getEscalationId()
    {
        return escalationId;
    }

    public void setEscalationId(UUID escalationId)
    {
        this.escalationId = escalationId;
    }

    public long getAfter()
    {
        return after;
    }

    public void setAfter(long after)
    {
        this.after = after;
    }

    public long getEscalatedAt()
    {
        return escalatedAt;
    }

    public void setEscalatedAt(long escalatedAt)
    {
        this.escalatedAt = escalatedAt;
    }

    public List<ContactMO> getNotified()
    {
        return notified;
    }

    public void setNotified(List<ContactMO> notified)
    {
        this.notified = notified;
    }
}
