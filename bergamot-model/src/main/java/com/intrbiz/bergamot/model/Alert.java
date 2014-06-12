package com.intrbiz.bergamot.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.data.db.compiler.meta.Action;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLForeignKey;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * An alert which was raised against a check
 */
@SQLTable(schema = BergamotDB.class, name = "alert", since = @SQLVersion({ 1, 0, 0 }))
public class Alert implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The unique ID for this check
     */
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey()
    private UUID id;

    @SQLColumn(index = 2, name = "site_id", notNull = true, since = @SQLVersion({ 1, 0, 0 }))
    @SQLForeignKey(references = Site.class, on = "id", onDelete = Action.CASCADE, onUpdate = Action.RESTRICT)
    private UUID siteId;

    /**
     * The check to which this alert was issued
     */
    @SQLColumn(index = 3, name = "check_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID checkId;

    /**
     * When was this alert raised
     */
    @SQLColumn(index = 4, name = "raised", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp raised;

    /**
     * Is the check ok?
     */
    @SQLColumn(index = 5, name = "ok", since = @SQLVersion({ 1, 0, 0 }))
    private boolean ok = true;

    /**
     * Why is the check ok or not ok?
     */
    @SQLColumn(index = 6, name = "status", since = @SQLVersion({ 1, 0, 0 }))
    private Status status = Status.PENDING;

    /**
     * What was the output of the last check
     */
    @SQLColumn(index = 7, name = "output", since = @SQLVersion({ 1, 0, 0 }))
    private String output = "Pending";

    /**
     * When did the last check happen
     */
    @SQLColumn(index = 8, name = "last_check_time", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp lastCheckTime = new Timestamp(System.currentTimeMillis());

    /**
     * What was the Id of the last check
     */
    @SQLColumn(index = 9, name = "last_check_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID lastCheckId;

    /**
     * The number of attempts since the last hard state change
     */
    @SQLColumn(index = 10, name = "attempt", since = @SQLVersion({ 1, 0, 0 }))
    private int attempt = Integer.MAX_VALUE;

    /**
     * Has a hard state transition happened
     */
    @SQLColumn(index = 11, name = "hard", since = @SQLVersion({ 1, 0, 0 }))
    private boolean hard = true;

    /**
     * Is the state in transition
     */
    @SQLColumn(index = 12, name = "transitioning", since = @SQLVersion({ 1, 0, 0 }))
    private boolean transitioning = false;

    /**
     * Is the state flapping between ok and not ok, but never reaching a hard state
     */
    @SQLColumn(index = 13, name = "flapping", since = @SQLVersion({ 1, 0, 0 }))
    private boolean flapping = false;

    /**
     * When was the last hard state change
     */
    @SQLColumn(index = 14, name = "last_state_change", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp lastStateChange = new Timestamp(System.currentTimeMillis());

    // history

    /**
     * A bitmap of the ok history
     */
    @SQLColumn(index = 15, name = "ok_history", since = @SQLVersion({ 1, 0, 0 }))
    private long okHistory = 0x1L;

    /**
     * Was the last hard state ok?
     */
    @SQLColumn(index = 16, name = "last_hard_ok", since = @SQLVersion({ 1, 0, 0 }))
    private boolean lastHardOk = true;

    /**
     * What was the last hard status?
     */
    @SQLColumn(index = 17, name = "last_hard_status", since = @SQLVersion({ 1, 0, 0 }))
    private Status lastHardStatus = Status.PENDING;

    /**
     * What was the output of the last hard state
     */
    @SQLColumn(index = 18, name = "last_hard_output", since = @SQLVersion({ 1, 0, 0 }))
    private String lastHardOutput = "Pending";

    /**
     * Has this alert been acknowledged by somebody
     */
    @SQLColumn(index = 19, name = "acknowledged", since = @SQLVersion({ 1, 0, 0 }))
    private boolean acknowledged = false;

    /**
     * When was this alert acknowledged
     */
    @SQLColumn(index = 20, name = "acknowledged_at", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp acknowledgedAt;

    /**
     * Who acknowledged this alert
     */
    @SQLColumn(index = 21, name = "acknowledged_by", since = @SQLVersion({ 1, 0, 0 }))
    private UUID acknowledgedBy;

    /**
     * Has this alert recovered by itself
     */
    @SQLColumn(index = 22, name = "recovered", since = @SQLVersion({ 1, 0, 0 }))
    private boolean recovered = false;

    /**
     * Which check execution caused this alert to recover
     */
    @SQLColumn(index = 23, name = "recovered_by", since = @SQLVersion({ 1, 0, 0 }))
    private UUID recoveredBy;

    /**
     * When did this check recover
     */
    @SQLColumn(index = 24, name = "recovered_at", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp recoveredAt;

    public Alert()
    {
        super();
    }

    public Alert(Check<?, ?> check, CheckState state)
    {
        this.siteId = check.getSiteId();
        this.id = Site.randomId(check.getSiteId());
        this.checkId = check.getId();
        this.raised = new Timestamp(System.currentTimeMillis());
        // copy the state
        this.attempt = state.getAttempt();
        this.lastCheckId = state.getLastCheckId();
        this.lastCheckTime = state.getLastCheckTime();
        this.lastHardOutput = state.getLastHardOutput();
        this.lastHardStatus = state.getLastHardStatus();
        this.lastStateChange = state.getLastStateChange();
        this.okHistory = state.getOkHistory();
        this.output = state.getOutput();
        this.status = state.getStatus();
        this.flapping = state.isFlapping();
        this.hard = state.isHard();
        this.lastHardOk = state.isLastHardOk();
        this.ok = state.isOk();
        this.transitioning = state.isTransitioning();
        // defaults for other state
        this.acknowledged = false;
        this.acknowledgedAt = null;
        this.acknowledgedBy = null;
        this.recovered = false;
        this.recoveredAt = null;
        this.recoveredBy = null;
    }

    public UUID getSiteId()
    {
        return siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public Timestamp getRaised()
    {
        return raised;
    }

    public void setRaised(Timestamp raised)
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

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
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

    public Timestamp getLastCheckTime()
    {
        return lastCheckTime;
    }

    public void setLastCheckTime(Timestamp lastCheckTime)
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

    public Timestamp getLastStateChange()
    {
        return lastStateChange;
    }

    public void setLastStateChange(Timestamp lastStateChange)
    {
        this.lastStateChange = lastStateChange;
    }

    public long getOkHistory()
    {
        return okHistory;
    }

    public void setOkHistory(long okHistory)
    {
        this.okHistory = okHistory;
    }

    public boolean isLastHardOk()
    {
        return lastHardOk;
    }

    public void setLastHardOk(boolean lastHardOk)
    {
        this.lastHardOk = lastHardOk;
    }

    public Status getLastHardStatus()
    {
        return lastHardStatus;
    }

    public void setLastHardStatus(Status lastHardStatus)
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

    public Timestamp getAcknowledgedAt()
    {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Timestamp acknowledgedAt)
    {
        this.acknowledgedAt = acknowledgedAt;
    }

    public UUID getAcknowledgedBy()
    {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(UUID acknowledgedBy)
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

    public Timestamp getRecoveredAt()
    {
        return recoveredAt;
    }

    public void setRecoveredAt(Timestamp recoveredAt)
    {
        this.recoveredAt = recoveredAt;
    }
}