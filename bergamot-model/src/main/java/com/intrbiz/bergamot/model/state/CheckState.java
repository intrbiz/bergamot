package com.intrbiz.bergamot.model.state;

import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.UUID;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.CheckCfg;
import com.intrbiz.bergamot.config.model.RealCheckCfg;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * The current state of a check
 * 
 * This is always the most current state of a check
 * 
 */
@SQLTable(schema = BergamotDB.class, name = "check_state", since = @SQLVersion({ 1, 0, 0 }))
public class CheckState extends BergamotObject<CheckStateMO> implements Cloneable
{
    private static final long serialVersionUID = 1L;
    
    @SQLColumn(index = 1, name = "check_id", since = @SQLVersion({ 1, 0, 0 }))
    @SQLPrimaryKey
    private UUID checkId;

    /**
     * Is the check ok?
     */
    @SQLColumn(index = 2, name = "ok", since = @SQLVersion({ 1, 0, 0 }))
    private boolean ok = true;

    /**
     * Why is the check ok or not ok?
     */
    @SQLColumn(index = 3, name = "status", since = @SQLVersion({ 1, 0, 0 }))
    private Status status = Status.PENDING;

    /**
     * What was the output of the last check
     */
    @SQLColumn(index = 4, name = "output", since = @SQLVersion({ 1, 0, 0 }))
    private String output = "Pending";

    /**
     * When did the last check happen
     */
    @SQLColumn(index = 5, name = "last_check_time", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp lastCheckTime = new Timestamp(System.currentTimeMillis());

    /**
     * What was the Id of the last check
     */
    @SQLColumn(index = 6, name = "last_check_id", since = @SQLVersion({ 1, 0, 0 }))
    private UUID lastCheckId;

    /**
     * The number of attempts since the last hard state change
     */
    @SQLColumn(index = 7, name = "attempt", since = @SQLVersion({ 1, 0, 0 }))
    private int attempt = 1;

    /**
     * Has a hard state transition happened
     */
    @SQLColumn(index = 8, name = "hard", since = @SQLVersion({ 1, 0, 0 }))
    private boolean hard = true;

    /**
     * Is the state in transition
     */
    @SQLColumn(index = 9, name = "transitioning", since = @SQLVersion({ 1, 0, 0 }))
    private boolean transitioning = false;

    /**
     * Is the state flapping between ok and not ok, but never reaching a hard state
     */
    @SQLColumn(index = 10, name = "flapping", since = @SQLVersion({ 1, 0, 0 }))
    private boolean flapping = false;

    /**
     * When was the last hard state change
     */
    @SQLColumn(index = 11, name = "last_state_change", since = @SQLVersion({ 1, 0, 0 }))
    private Timestamp lastStateChange = new Timestamp(System.currentTimeMillis());

    // history

    /**
     * A bitmap of the ok history
     */
    @SQLColumn(index = 12, name = "ok_history", since = @SQLVersion({ 1, 0, 0 }))
    private long okHistory = 0x1L;

    /**
     * Was the last hard state ok?
     */
    @SQLColumn(index = 13, name = "last_hard_ok", since = @SQLVersion({ 1, 0, 0 }))
    private boolean lastHardOk = true;

    /**
     * What was the last hard status?
     */
    @SQLColumn(index = 14, name = "last_hard_status", since = @SQLVersion({ 1, 0, 0 }))
    private Status lastHardStatus = Status.PENDING;

    /**
     * What was the output of the last hard state
     */
    @SQLColumn(index = 15, name = "last_hard_output", since = @SQLVersion({ 1, 0, 0 }))
    private String lastHardOutput = "Pending";

    /**
     * Is this check currently in downtime
     */
    @SQLColumn(index = 16, name = "in_downtime", since = @SQLVersion({ 3, 3, 0 }))
    private boolean inDowntime;
    
    /**
     * Is this check currently suppressed
     */
    @SQLColumn(index = 17, name = "suppressed", since = @SQLVersion({ 3, 4, 0 }))
    private boolean suppressed;

    public CheckState()
    {
        super();
    }

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
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

    /**
     * Are we in a hard state
     */
    public boolean isHard()
    {
        return hard;
    }
    
    /**
     * Are we in a soft state, the opposite of a hard state
     */
    public boolean isSoft()
    {
        return ! hard;
    }
    
    /**
     * Are we in a hard OK state, IE: this.hard && this.ok
     */
    public boolean isHardOk()
    {
        return this.hard && this.ok;
    }

    public void setHard(boolean hard)
    {
        this.hard = hard;
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

    public void pushOkHistory(boolean ok)
    {
        this.okHistory = ((this.okHistory << 1) & 0x7FFFFFFFFFFFFFFFL) | (ok ? 1L : 0L);
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

    /**
     * Is this check currently in downtime
     */
    public boolean isInDowntime()
    {
        return this.inDowntime;
    }

    public void setInDowntime(boolean inDowntime)
    {
        this.inDowntime = inDowntime;
    }

    public boolean isSuppressed()
    {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
    }
    
    public boolean isSuppressedOrInDowntime()
    {
        return this.suppressed || this.inDowntime;
    }

    @Override
    public CheckStateMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        CheckStateMO mo = new CheckStateMO();
        mo.setAttempt(this.getAttempt());
        mo.setFlapping(this.isFlapping());
        mo.setHard(this.isHard());
        mo.setLastCheckId(this.getLastCheckId());
        mo.setLastCheckTime(this.getLastCheckTime().getTime());
        mo.setLastStateChange(this.getLastStateChange().getTime());
        mo.setOk(this.isOk());
        mo.setOutput(this.getOutput());
        mo.setStatus(this.getStatus().toString());
        mo.setTransitioning(this.isTransitioning());
        mo.setLastHardOk(this.isLastHardOk());
        mo.setLastHardStatus(this.getLastHardStatus().toString());
        mo.setLastHardOutput(this.getLastHardOutput());
        mo.setInDowntime(this.isInDowntime());
        mo.setSuppressed(this.isSuppressed());
        return mo;
    }
    
    public void configure(CheckCfg<?> cfg)
    {
        if (cfg.getInitialState() != null)
        {
            this.setStatus(Status.valueOf(cfg.getInitialState().getStatus().toUpperCase()));
            this.setOk(this.getStatus().isOk());
            this.setOutput(Util.coalesce(cfg.getInitialState().getOutput(), ""));
            this.setLastHardStatus(this.getStatus());
            this.setLastHardOk(this.isOk());
            this.setLastHardOutput(this.getOutput());
        }
        // set the attempt
        if (cfg instanceof RealCheckCfg && ((RealCheckCfg<?>)cfg).getState() != null)
        {
            this.setAttempt(((RealCheckCfg<?>)cfg).getState().getRecoversAfter());
        }
        // update last check time
        this.setLastCheckTime(new Timestamp(System.currentTimeMillis()));
    }
    
    public String toString()
    {
        return "CheckState { check => " + this.checkId + ", ok => " + this.ok + ", status => " + this.status + ", output => " + this.output + ", attempt => " + this.attempt + ", hard => " + this.hard + " }";
    }
    
    public CheckState clone()
    {
        try
        {
            return (CheckState) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
