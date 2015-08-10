package com.intrbiz.bergamot.model.state;

import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.UUID;

import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.BergamotObject;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.message.state.CheckTransitionMO;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLPrimaryKey;
import com.intrbiz.data.db.compiler.meta.SQLTable;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A log of the transition caused by each check 
 * result which has been applied to a check state.
 * 
 * Every check execution returns a check result, 
 * this result is then applied to the current state 
 * of a check, returning a transition.
 * 
 */
@SQLTable(schema = BergamotDB.class, name = "check_transition", since = @SQLVersion({ 1, 3, 0 }))
public class CheckTransition extends BergamotObject<CheckTransitionMO>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The transition id, this should match the id of the execution / result
     */
    @SQLColumn(index = 1, name = "id", since = @SQLVersion({ 1, 3, 0 }))
    @SQLPrimaryKey
    private UUID id;
    
    /**
     * The check to which this transition applies
     */
    @SQLColumn(index = 2, name = "check_id", since = @SQLVersion({ 1, 3, 0 }))
    private UUID checkId;
    
    /**
     * When the transition was applied
     */
    @SQLColumn(index = 3, name = "applied_at", since = @SQLVersion({ 1, 3, 0 }))
    private Timestamp appliedAt = new Timestamp(System.currentTimeMillis());
    
    /**
     * Did this transition result in a change of state for the check
     */
    @SQLColumn(index = 4, name = "state_change", since = @SQLVersion({ 1, 3, 0 }))
    private boolean stateChange;
    
    /**
     * Did this transition result in a hard change.  IE the check state reached 
     * the attempt threshold which caused a hard change in state.
     */
    @SQLColumn(index = 5, name = "hard_change", since = @SQLVersion({ 1, 3, 0 }))
    private boolean hardChange;
    
    // previous

    /**
     * Previous State: Is the check ok?
     */
    @SQLColumn(index = 6, name = "previous_ok", since = @SQLVersion({ 1, 3, 0 }))
    private boolean previousOk;

    /**
     * Previous State: Why is the check ok or not ok?
     */
    @SQLColumn(index = 7, name = "previous_status", since = @SQLVersion({ 1, 3, 0 }))
    private Status previousStatus;

    /**
     * Previous State: What was the output of the last check
     */
    @SQLColumn(index = 8, name = "previous_output", since = @SQLVersion({ 1, 3, 0 }))
    private String previousOutput;

    /**
     * Previous State: When did the last check happen
     */
    @SQLColumn(index = 9, name = "previous_last_check_time", since = @SQLVersion({ 1, 3, 0 }))
    private Timestamp previousLastCheckTime;

    /**
     * Previous State: What was the Id of the last check
     */
    @SQLColumn(index = 10, name = "previous_last_check_id", since = @SQLVersion({ 1, 3, 0 }))
    private UUID previousLastCheckId;

    /**
     * Previous State: The number of attempts since the last hard state change
     */
    @SQLColumn(index = 11, name = "previous_attempt", since = @SQLVersion({ 1, 3, 0 }))
    private int previousAttempt;

    /**
     * Previous State: Has a hard state transition happened
     */
    @SQLColumn(index = 12, name = "previous_hard", since = @SQLVersion({ 1, 3, 0 }))
    private boolean previousHard;

    /**
     * Previous State: Is the state in transition
     */
    @SQLColumn(index = 13, name = "previous_transitioning", since = @SQLVersion({ 1, 3, 0 }))
    private boolean previousTransitioning;

    /**
     * Previous State: Is the state flapping between ok and not ok, but never reaching a hard state
     */
    @SQLColumn(index = 14, name = "previous_flapping", since = @SQLVersion({ 1, 3, 0 }))
    private boolean previousFlapping;

    /**
     * Previous State: When was the last hard state change
     */
    @SQLColumn(index = 15, name = "previous_last_state_change", since = @SQLVersion({ 1, 3, 0 }))
    private Timestamp previousLastStateChange;

    /**
     * Previous State: A bitmap of the ok history
     */
    @SQLColumn(index = 16, name = "previous_ok_history", since = @SQLVersion({ 1, 3, 0 }))
    private long previousOkHistory = 0x1L;

    /**
     * Previous State: Was the last hard state ok?
     */
    @SQLColumn(index = 17, name = "previous_last_hard_ok", since = @SQLVersion({ 1, 3, 0 }))
    private boolean previousLastHardOk = true;

    /**
     * Previous State: What was the last hard status?
     */
    @SQLColumn(index = 18, name = "previous_last_hard_status", since = @SQLVersion({ 1, 3, 0 }))
    private Status previousLastHardStatus = Status.PENDING;

    /**
     * Previous State: What was the output of the last hard state
     */
    @SQLColumn(index = 19, name = "previous_last_hard_output", since = @SQLVersion({ 1, 3, 0 }))
    private String previousLastHardOutput = "Pending";  
 
    // next
    
    /**
     * Next State: Is the check ok?
     */
    @SQLColumn(index = 20, name = "next_ok", since = @SQLVersion({ 1, 3, 0 }))
    private boolean nextOk;

    /**
     * Next State: Why is the check ok or not ok?
     */
    @SQLColumn(index = 21, name = "next_status", since = @SQLVersion({ 1, 3, 0 }))
    private Status nextStatus;

    /**
     * Next State: What was the output of the last check
     */
    @SQLColumn(index = 22, name = "next_output", since = @SQLVersion({ 1, 3, 0 }))
    private String nextOutput;

    /**
     * Next State: When did the last check happen
     */
    @SQLColumn(index = 23, name = "next_last_check_time", since = @SQLVersion({ 1, 3, 0 }))
    private Timestamp nextLastCheckTime;

    /**
     * Next State: What was the Id of the last check
     */
    @SQLColumn(index = 24, name = "next_last_check_id", since = @SQLVersion({ 1, 3, 0 }))
    private UUID nextLastCheckId;

    /**
     * Next State: The number of attempts since the last hard state change
     */
    @SQLColumn(index = 25, name = "next_attempt", since = @SQLVersion({ 1, 3, 0 }))
    private int nextAttempt;

    /**
     * Next State: Has a hard state transition happened
     */
    @SQLColumn(index = 26, name = "next_hard", since = @SQLVersion({ 1, 3, 0 }))
    private boolean nextHard;

    /**
     * Next State: Is the state in transition
     */
    @SQLColumn(index = 27, name = "next_transitioning", since = @SQLVersion({ 1, 3, 0 }))
    private boolean nextTransitioning;

    /**
     * Next State: Is the state flapping between ok and not ok, but never reaching a hard state
     */
    @SQLColumn(index = 28, name = "next_flapping", since = @SQLVersion({ 1, 3, 0 }))
    private boolean nextFlapping;

    /**
     * Next State: When was the last hard state change
     */
    @SQLColumn(index = 29, name = "next_last_state_change", since = @SQLVersion({ 1, 3, 0 }))
    private Timestamp nextLastStateChange;

    /**
     * Next State: A bitmap of the ok history
     */
    @SQLColumn(index = 30, name = "next_ok_history", since = @SQLVersion({ 1, 3, 0 }))
    private long nextOkHistory = 0x1L;

    /**
     * Next State: Was the last hard state ok?
     */
    @SQLColumn(index = 31, name = "next_last_hard_ok", since = @SQLVersion({ 1, 3, 0 }))
    private boolean nextLastHardOk = true;

    /**
     * Next State: What was the last hard status?
     */
    @SQLColumn(index = 32, name = "next_last_hard_status", since = @SQLVersion({ 1, 3, 0 }))
    private Status nextLastHardStatus = Status.PENDING;

    /**
     * Next State: What was the output of the last hard state
     */
    @SQLColumn(index = 33, name = "next_last_hard_output", since = @SQLVersion({ 1, 3, 0 }))
    private String nextLastHardOutput = "Pending";
    
    /**
     * Does this transition result in an alert, IE: A hard change from ok to not ok.
     */
    @SQLColumn(index = 34, name = "alert", since = @SQLVersion({ 1, 7, 0 }))
    private boolean alert;
    
    /**
     * Does this transition result in a recovery, IE: A hard change from not ok to ok.
     */
    @SQLColumn(index = 35, name = "recovery", since = @SQLVersion({ 1, 7, 0 }))
    private boolean recovery;

    /**
     * Previous State: Was the check previously in downtime
     */
    @SQLColumn(index = 36, name = "previous_in_downtime", since = @SQLVersion({ 3, 3, 0 }))
    private boolean previousInDowntime;

    /**
     * Next State: Is the check now in downtime
     */
    @SQLColumn(index = 37, name = "next_in_downtime", since = @SQLVersion({ 3, 3, 0 }))
    private boolean nextInDowntime;
    
    /**
     * Previous State: Was the check previously suppressed
     */
    @SQLColumn(index = 38, name = "previous_suppressed", since = @SQLVersion({ 3, 4, 0 }))
    private boolean previousSuppressed;

    /**
     * Next State: Is the check now suppressed
     */
    @SQLColumn(index = 39, name = "next_suppressed", since = @SQLVersion({ 3, 4, 0 }))
    private boolean nextSuppressed;
    
    public CheckTransition()
    {
        super();
    }
    
    public CheckTransition(UUID id, UUID checkId, Timestamp appliedAt, boolean stateChange, boolean hardChange, boolean alert, boolean recovery, CheckState previous, CheckState next)
    {
        super();
        this.id = id;
        this.checkId = checkId;
        this.appliedAt = appliedAt;
        this.stateChange = stateChange;
        this.hardChange = hardChange;
        this.alert = alert;
        this.recovery = recovery;
        this.fromPreviousState(previous);
        this.fromNextState(next);
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

    public Timestamp getAppliedAt()
    {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt)
    {
        this.appliedAt = appliedAt;
    }

    public boolean isStateChange()
    {
        return stateChange;
    }

    public void setStateChange(boolean stateChange)
    {
        this.stateChange = stateChange;
    }

    public boolean isHardChange()
    {
        return hardChange;
    }

    public void setHardChange(boolean hardChange)
    {
        this.hardChange = hardChange;
    }

    public boolean isPreviousOk()
    {
        return previousOk;
    }

    public void setPreviousOk(boolean previousOk)
    {
        this.previousOk = previousOk;
    }

    public Status getPreviousStatus()
    {
        return previousStatus;
    }

    public void setPreviousStatus(Status previousStatus)
    {
        this.previousStatus = previousStatus;
    }

    public String getPreviousOutput()
    {
        return previousOutput;
    }

    public void setPreviousOutput(String previousOutput)
    {
        this.previousOutput = previousOutput;
    }

    public Timestamp getPreviousLastCheckTime()
    {
        return previousLastCheckTime;
    }

    public void setPreviousLastCheckTime(Timestamp previousLastCheckTime)
    {
        this.previousLastCheckTime = previousLastCheckTime;
    }

    public UUID getPreviousLastCheckId()
    {
        return previousLastCheckId;
    }

    public void setPreviousLastCheckId(UUID previousLastCheckId)
    {
        this.previousLastCheckId = previousLastCheckId;
    }

    public int getPreviousAttempt()
    {
        return previousAttempt;
    }

    public void setPreviousAttempt(int previousAttempt)
    {
        this.previousAttempt = previousAttempt;
    }

    public boolean isPreviousHard()
    {
        return previousHard;
    }

    public void setPreviousHard(boolean previousHard)
    {
        this.previousHard = previousHard;
    }

    public boolean isPreviousTransitioning()
    {
        return previousTransitioning;
    }

    public void setPreviousTransitioning(boolean previousTransitioning)
    {
        this.previousTransitioning = previousTransitioning;
    }

    public boolean isPreviousFlapping()
    {
        return previousFlapping;
    }

    public void setPreviousFlapping(boolean previousFlapping)
    {
        this.previousFlapping = previousFlapping;
    }

    public Timestamp getPreviousLastStateChange()
    {
        return previousLastStateChange;
    }

    public void setPreviousLastStateChange(Timestamp previousLastStateChange)
    {
        this.previousLastStateChange = previousLastStateChange;
    }

    public long getPreviousOkHistory()
    {
        return previousOkHistory;
    }

    public void setPreviousOkHistory(long previousOkHistory)
    {
        this.previousOkHistory = previousOkHistory;
    }

    public boolean isPreviousLastHardOk()
    {
        return previousLastHardOk;
    }

    public void setPreviousLastHardOk(boolean previousLastHardOk)
    {
        this.previousLastHardOk = previousLastHardOk;
    }

    public Status getPreviousLastHardStatus()
    {
        return previousLastHardStatus;
    }

    public void setPreviousLastHardStatus(Status previousLastHardStatus)
    {
        this.previousLastHardStatus = previousLastHardStatus;
    }

    public String getPreviousLastHardOutput()
    {
        return previousLastHardOutput;
    }

    public void setPreviousLastHardOutput(String previousLastHardOutput)
    {
        this.previousLastHardOutput = previousLastHardOutput;
    }

    public boolean isNextOk()
    {
        return nextOk;
    }

    public void setNextOk(boolean nextOk)
    {
        this.nextOk = nextOk;
    }

    public Status getNextStatus()
    {
        return nextStatus;
    }

    public void setNextStatus(Status nextStatus)
    {
        this.nextStatus = nextStatus;
    }

    public String getNextOutput()
    {
        return nextOutput;
    }

    public void setNextOutput(String nextOutput)
    {
        this.nextOutput = nextOutput;
    }

    public Timestamp getNextLastCheckTime()
    {
        return nextLastCheckTime;
    }

    public void setNextLastCheckTime(Timestamp nextLastCheckTime)
    {
        this.nextLastCheckTime = nextLastCheckTime;
    }

    public UUID getNextLastCheckId()
    {
        return nextLastCheckId;
    }

    public void setNextLastCheckId(UUID nextLastCheckId)
    {
        this.nextLastCheckId = nextLastCheckId;
    }

    public int getNextAttempt()
    {
        return nextAttempt;
    }

    public void setNextAttempt(int nextAttempt)
    {
        this.nextAttempt = nextAttempt;
    }

    public boolean isNextHard()
    {
        return nextHard;
    }

    public void setNextHard(boolean nextHard)
    {
        this.nextHard = nextHard;
    }

    public boolean isNextTransitioning()
    {
        return nextTransitioning;
    }

    public void setNextTransitioning(boolean nextTransitioning)
    {
        this.nextTransitioning = nextTransitioning;
    }

    public boolean isNextFlapping()
    {
        return nextFlapping;
    }

    public void setNextFlapping(boolean nextFlapping)
    {
        this.nextFlapping = nextFlapping;
    }

    public Timestamp getNextLastStateChange()
    {
        return nextLastStateChange;
    }

    public void setNextLastStateChange(Timestamp nextLastStateChange)
    {
        this.nextLastStateChange = nextLastStateChange;
    }

    public long getNextOkHistory()
    {
        return nextOkHistory;
    }

    public void setNextOkHistory(long nextOkHistory)
    {
        this.nextOkHistory = nextOkHistory;
    }

    public boolean isNextLastHardOk()
    {
        return nextLastHardOk;
    }

    public void setNextLastHardOk(boolean nextLastHardOk)
    {
        this.nextLastHardOk = nextLastHardOk;
    }

    public Status getNextLastHardStatus()
    {
        return nextLastHardStatus;
    }

    public void setNextLastHardStatus(Status nextLastHardStatus)
    {
        this.nextLastHardStatus = nextLastHardStatus;
    }

    public String getNextLastHardOutput()
    {
        return nextLastHardOutput;
    }

    public void setNextLastHardOutput(String nextLastHardOutput)
    {
        this.nextLastHardOutput = nextLastHardOutput;
    }
    
    public boolean isAlert()
    {
        return alert;
    }

    public void setAlert(boolean alert)
    {
        this.alert = alert;
    }

    public boolean isRecovery()
    {
        return recovery;
    }

    public void setRecovery(boolean recovery)
    {
        this.recovery = recovery;
    }

    public boolean isPreviousInDowntime()
    {
        return this.previousInDowntime;
    }

    public void setPreviousInDowntime(boolean previousInDowntime)
    {
        this.previousInDowntime = previousInDowntime;
    }

    public boolean isNextInDowntime()
    {
        return this.nextInDowntime;
    }

    public void setNextInDowntime(boolean nextInDowntime)
    {
        this.nextInDowntime = nextInDowntime;
    }

    public boolean isPreviousSuppressed()
    {
        return previousSuppressed;
    }

    public void setPreviousSuppressed(boolean previousSuppressed)
    {
        this.previousSuppressed = previousSuppressed;
    }

    public boolean isNextSuppressed()
    {
        return nextSuppressed;
    }

    public void setNextSuppressed(boolean nextSuppressed)
    {
        this.nextSuppressed = nextSuppressed;
    }
    
    // helpers

    public CheckState toPreviousState()
    {
        CheckState state = new CheckState();
        state.setCheckId(this.checkId);
        state.setAttempt(this.previousAttempt);
        state.setFlapping(this.previousFlapping);
        state.setHard(this.previousHard);
        state.setLastCheckId(this.previousLastCheckId);
        state.setLastCheckTime(this.previousLastCheckTime);
        state.setLastHardOk(this.previousLastHardOk);
        state.setLastHardOutput(this.previousLastHardOutput);
        state.setLastHardStatus(this.previousLastHardStatus);
        state.setLastStateChange(this.previousLastStateChange);
        state.setOk(this.previousOk);
        state.setOkHistory(this.previousOkHistory);
        state.setOutput(this.previousOutput);
        state.setStatus(this.previousStatus);
        state.setTransitioning(this.previousTransitioning);
        state.setInDowntime(this.previousInDowntime);
        state.setSuppressed(this.previousSuppressed);
        return state;
    }
    
    public void fromPreviousState(CheckState state)
    {
        this.previousAttempt = state.getAttempt();
        this.previousFlapping = state.isFlapping();
        this.previousHard = state.isHard();
        this.previousLastCheckId = state.getLastCheckId();
        this.previousLastCheckTime = state.getLastCheckTime();
        this.previousLastHardOk = state.isLastHardOk();
        this.previousLastHardOutput = state.getLastHardOutput();
        this.previousLastHardStatus = state.getLastHardStatus();
        this.previousLastStateChange = state.getLastStateChange();
        this.previousOk = state.isOk();
        this.previousOkHistory = state.getOkHistory();
        this.previousOutput = state.getOutput();
        this.previousStatus = state.getStatus();
        this.previousTransitioning = state.isTransitioning();
        this.previousInDowntime = state.isInDowntime();
        this.previousSuppressed = state.isSuppressed();
    }
    
    public CheckState toNextState()
    {
        CheckState state = new CheckState();
        state.setCheckId(this.checkId);
        state.setAttempt(this.nextAttempt);
        state.setFlapping(this.nextFlapping);
        state.setHard(this.nextHard);
        state.setLastCheckId(this.nextLastCheckId);
        state.setLastCheckTime(this.nextLastCheckTime);
        state.setLastHardOk(this.nextLastHardOk);
        state.setLastHardOutput(this.nextLastHardOutput);
        state.setLastHardStatus(this.nextLastHardStatus);
        state.setLastStateChange(this.nextLastStateChange);
        state.setOk(this.nextOk);
        state.setOkHistory(this.nextOkHistory);
        state.setOutput(this.nextOutput);
        state.setStatus(this.nextStatus);
        state.setTransitioning(this.nextTransitioning);
        state.setInDowntime(this.nextInDowntime);
        state.setSuppressed(this.nextSuppressed);
        return state;
    }
    
    public void fromNextState(CheckState state)
    {
        this.nextAttempt = state.getAttempt();
        this.nextFlapping = state.isFlapping();
        this.nextHard = state.isHard();
        this.nextLastCheckId = state.getLastCheckId();
        this.nextLastCheckTime = state.getLastCheckTime();
        this.nextLastHardOk = state.isLastHardOk();
        this.nextLastHardOutput = state.getLastHardOutput();
        this.nextLastHardStatus = state.getLastHardStatus();
        this.nextLastStateChange = state.getLastStateChange();
        this.nextOk = state.isOk();
        this.nextOkHistory = state.getOkHistory();
        this.nextOutput = state.getOutput();
        this.nextStatus = state.getStatus();
        this.nextTransitioning = state.isTransitioning();
        this.nextInDowntime = state.isInDowntime();
        this.nextSuppressed = state.isSuppressed();
    }
    
    @Override
    public CheckTransitionMO toMO(Contact contact, EnumSet<MOFlag> options)
    {
        CheckTransitionMO mo = new CheckTransitionMO();
        mo.setId(this.getId());
        mo.setCheckId(this.getCheckId());
        mo.setAppliedAt(this.getAppliedAt().getTime());
        mo.setStateChange(this.isStateChange());
        mo.setHardChange(this.isHardChange());
        mo.setPreviousOk(this.isPreviousOk());
        mo.setPreviousStatus(this.getPreviousStatus().toString());
        mo.setPreviousOutput(this.getPreviousOutput());
        mo.setPreviousLastCheckTime(this.getPreviousLastCheckTime().getTime());
        mo.setPreviousLastCheckId(this.getPreviousLastCheckId());
        mo.setPreviousAttempt(this.getPreviousAttempt());
        mo.setPreviousHard(this.isPreviousHard());
        mo.setPreviousTransitioning(this.isPreviousTransitioning());
        mo.setPreviousFlapping(this.isPreviousFlapping());
        mo.setPreviousLastStateChange(this.getPreviousLastStateChange().getTime());
        mo.setPreviousOkHistory(this.getPreviousOkHistory());
        mo.setPreviousLastHardOk(this.isPreviousLastHardOk());
        mo.setPreviousLastHardStatus(this.getPreviousLastHardStatus().toString());
        mo.setPreviousLastHardOutput(this.getPreviousLastHardOutput());
        mo.setPreviousInDowntime(this.isPreviousInDowntime());
        mo.setPreviousSuppressed(this.isPreviousSuppressed());
        mo.setNextOk(this.isNextOk());
        mo.setNextStatus(this.getNextStatus().toString());
        mo.setNextOutput(this.getNextOutput());
        mo.setNextLastCheckTime(this.getNextLastCheckTime().getTime());
        mo.setNextLastCheckId(this.getNextLastCheckId());
        mo.setNextAttempt(this.getNextAttempt());
        mo.setNextHard(this.isNextHard());
        mo.setNextTransitioning(this.isNextTransitioning());
        mo.setNextFlapping(this.isNextFlapping());
        mo.setNextLastStateChange(this.getNextLastStateChange().getTime());
        mo.setNextOkHistory(this.getNextOkHistory());
        mo.setNextLastHardOk(this.isNextLastHardOk());
        mo.setNextLastHardStatus(this.getNextLastHardStatus().toString());
        mo.setNextLastHardOutput(this.getNextLastHardOutput());
        mo.setNextInDowntime(this.isNextInDowntime());
        mo.setNextSuppressed(this.isNextSuppressed());
        mo.setAlert(this.isAlert());
        mo.setRecovery(this.isRecovery());
        return mo;
    }
}
