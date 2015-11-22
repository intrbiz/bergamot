package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.alert")
public class AlertMO extends MessageObject implements CommentedMO
{
    /**
     * The alert id
     */
    @JsonProperty("id")
    private UUID id;

    /**
     * The check this alert is for
     */
    @JsonProperty("check")
    private CheckMO check;

    /**
     * When the alert was raised
     */
    @JsonProperty("raised")
    private long raised;

    /**
     * Is the check ok?
     */
    @JsonProperty("ok")
    private boolean ok;

    /**
     * Why is the check ok or not ok?
     */
    @JsonProperty("status")
    private String status;

    /**
     * What was the output of the last check
     */
    @JsonProperty("output")
    private String output;

    /**
     * When did the last check happen
     */
    @JsonProperty("last_check_time")
    private long lastCheckTime;

    /**
     * What was the Id of the last check
     */
    @JsonProperty("last_check_id")
    private UUID lastCheckId;

    /**
     * The number of attempts since the last hard state change
     */
    @JsonProperty("attempt")
    private int attempt;

    /**
     * Has a hard state transition happened
     */
    @JsonProperty("hard")
    private boolean hard;

    /**
     * Is the state in transition
     */
    @JsonProperty("transitioning")
    private boolean transitioning;

    /**
     * Is the state flapping between ok and not ok, but never reaching a hard state
     */
    @JsonProperty("flapping")
    private boolean flapping;

    /**
     * When was the last hard state change
     */
    @JsonProperty("last_state_change")
    private long lastStateChange;

    // history

    /**
     * Was the last hard state ok?
     */
    @JsonProperty("last_hard_ok")
    private boolean lastHardOk;

    /**
     * What was the last hard status?
     */
    @JsonProperty("last_hard_status")
    private String lastHardStatus;

    /**
     * What was the output of the last hard state
     */
    @JsonProperty("last_hard_output")
    private String lastHardOutput;

    /**
     * Has this alert been acknowledged by somebody
     */
    @JsonProperty("acknowledged")
    private boolean acknowledged = false;

    /**
     * When was this alert acknowledged
     */
    @JsonProperty("acknowleged_at")
    private long acknowledgedAt;

    /**
     * Whom acknowledged this alert
     */
    @JsonProperty("acknowleged_by")
    private ContactMO acknowledgedBy;

    /**
     * Has this alert recovered by itself
     */
    @JsonProperty("recovered")
    private boolean recovered = false;

    /**
     * Which check execution caused this alert to recover
     */
    @JsonProperty("recovered_by")
    private UUID recoveredBy;

    /**
     * When did this check recover
     */
    @JsonProperty("recovered_at")
    private long recoveredAt;

    @JsonProperty("comments")
    private List<CommentMO> comments = new LinkedList<CommentMO>();

    @JsonProperty("escalated")
    private boolean escalated;

    @JsonProperty("escalated_at")
    private long escalatedAt;

    @JsonProperty("escalations")
    private List<AlertEscalationMO> escalations = new LinkedList<AlertEscalationMO>();

    @JsonProperty("notified")
    private List<ContactMO> notified = new LinkedList<ContactMO>();
    
    @JsonProperty("encompassed")
    private List<AlertEncompassesMO> encompassed = new LinkedList<AlertEncompassesMO>();

    public AlertMO()
    {
        super();
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public CheckMO getCheck()
    {
        return check;
    }

    public void setCheck(CheckMO check)
    {
        this.check = check;
    }

    public long getRaised()
    {
        return raised;
    }

    public void setRaised(long raised)
    {
        this.raised = raised;
    }

    public boolean isOk()
    {
        return ok;
    }

    public void setOk(boolean ok)
    {
        this.ok = ok;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getOutput()
    {
        return output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public long getLastCheckTime()
    {
        return lastCheckTime;
    }

    public void setLastCheckTime(long lastCheckTime)
    {
        this.lastCheckTime = lastCheckTime;
    }

    public UUID getLastCheckId()
    {
        return lastCheckId;
    }

    public void setLastCheckId(UUID lastCheckId)
    {
        this.lastCheckId = lastCheckId;
    }

    public int getAttempt()
    {
        return attempt;
    }

    public void setAttempt(int attempt)
    {
        this.attempt = attempt;
    }

    public boolean isHard()
    {
        return hard;
    }

    public void setHard(boolean hard)
    {
        this.hard = hard;
    }

    public boolean isTransitioning()
    {
        return transitioning;
    }

    public void setTransitioning(boolean transitioning)
    {
        this.transitioning = transitioning;
    }

    public boolean isFlapping()
    {
        return flapping;
    }

    public void setFlapping(boolean flapping)
    {
        this.flapping = flapping;
    }

    public long getLastStateChange()
    {
        return lastStateChange;
    }

    public void setLastStateChange(long lastStateChange)
    {
        this.lastStateChange = lastStateChange;
    }

    public boolean isLastHardOk()
    {
        return lastHardOk;
    }

    public void setLastHardOk(boolean lastHardOk)
    {
        this.lastHardOk = lastHardOk;
    }

    public String getLastHardStatus()
    {
        return lastHardStatus;
    }

    public void setLastHardStatus(String lastHardStatus)
    {
        this.lastHardStatus = lastHardStatus;
    }

    public String getLastHardOutput()
    {
        return lastHardOutput;
    }

    public void setLastHardOutput(String lastHardOutput)
    {
        this.lastHardOutput = lastHardOutput;
    }

    public boolean isAcknowledged()
    {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged)
    {
        this.acknowledged = acknowledged;
    }

    public long getAcknowledgedAt()
    {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(long acknowledgedAt)
    {
        this.acknowledgedAt = acknowledgedAt;
    }

    public ContactMO getAcknowledgedBy()
    {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(ContactMO acknowledgedBy)
    {
        this.acknowledgedBy = acknowledgedBy;
    }

    public boolean isRecovered()
    {
        return recovered;
    }

    public void setRecovered(boolean recovered)
    {
        this.recovered = recovered;
    }

    public UUID getRecoveredBy()
    {
        return recoveredBy;
    }

    public void setRecoveredBy(UUID recoveredBy)
    {
        this.recoveredBy = recoveredBy;
    }

    public long getRecoveredAt()
    {
        return recoveredAt;
    }

    public void setRecoveredAt(long recoveredAt)
    {
        this.recoveredAt = recoveredAt;
    }

    @Override
    public List<CommentMO> getComments()
    {
        return comments;
    }

    @Override
    public void setComments(List<CommentMO> comments)
    {
        this.comments = comments;
    }

    public boolean isEscalated()
    {
        return escalated;
    }

    public void setEscalated(boolean escalated)
    {
        this.escalated = escalated;
    }

    public long getEscalatedAt()
    {
        return escalatedAt;
    }

    public void setEscalatedAt(long escalatedAt)
    {
        this.escalatedAt = escalatedAt;
    }

    public List<AlertEscalationMO> getEscalations()
    {
        return escalations;
    }

    public void setEscalations(List<AlertEscalationMO> escalations)
    {
        this.escalations = escalations;
    }

    public List<ContactMO> getNotified()
    {
        return notified;
    }

    public void setNotified(List<ContactMO> notified)
    {
        this.notified = notified;
    }

    public List<AlertEncompassesMO> getEncompassed()
    {
        return encompassed;
    }

    public void setEncompassed(List<AlertEncompassesMO> encompassed)
    {
        this.encompassed = encompassed;
    }
}
