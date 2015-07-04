package com.intrbiz.bergamot.model.message.state;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * A log of the transition caused by each check 
 * result which has been applied to a check state.
 * 
 * Every check execution returns a check result, 
 * this result is then applied to the current state 
 * of a check, returning a transition.
 * 
 */
@JsonTypeName("bergamot.transition.check")
public class CheckTransitionMO extends MessageObject
{    
    /**
     * The transition id, this should match the id of the execution / result
     */
    private UUID id;
    
    /**
     * The check to which this transition applies
     */
    private UUID checkId;
    
    /**
     * When the transition was applied
     */
    private long appliedAt;
    
    /**
     * Did this transition result in a change of state for the check
     */
    private boolean stateChange;
    
    /**
     * Did this transition result in a hard change.  IE the check state reached 
     * the attempt threshold which caused a hard change in state.
     */
    private boolean hardChange;
    
    // previous

    /**
     * Previous State: Is the check ok?
     */
    private boolean previousOk;

    /**
     * Previous State: Why is the check ok or not ok?
     */
    private String previousStatus;

    /**
     * Previous State: What was the output of the last check
     */
    private String previousOutput;

    /**
     * Previous State: When did the last check happen
     */
    private long previousLastCheckTime;

    /**
     * Previous State: What was the Id of the last check
     */
    private UUID previousLastCheckId;

    /**
     * Previous State: The number of attempts since the last hard state change
     */
    private int previousAttempt;

    /**
     * Previous State: Has a hard state transition happened
     */
    private boolean previousHard;

    /**
     * Previous State: Is the state in transition
     */
    private boolean previousTransitioning;

    /**
     * Previous State: Is the state flapping between ok and not ok, but never reaching a hard state
     */
    private boolean previousFlapping;

    /**
     * Previous State: When was the last hard state change
     */
    private long previousLastStateChange;

    /**
     * Previous State: A bitmap of the ok history
     */
    private long previousOkHistory;

    /**
     * Previous State: Was the last hard state ok?
     */
    private boolean previousLastHardOk;

    /**
     * Previous State: What was the last hard status?
     */
    private String previousLastHardStatus;

    /**
     * Previous State: What was the output of the last hard state
     */
    private String previousLastHardOutput = "Pending";
    
    // next
    
    /**
     * Next State: Is the check ok?
     */
    private boolean nextOk;

    /**
     * Next State: Why is the check ok or not ok?
     */
    private String nextStatus;

    /**
     * Next State: What was the output of the last check
     */
    private String nextOutput;

    /**
     * Next State: When did the last check happen
     */
    private long nextLastCheckTime;

    /**
     * Next State: What was the Id of the last check
     */
    private UUID nextLastCheckId;

    /**
     * Next State: The number of attempts since the last hard state change
     */
    private int nextAttempt;

    /**
     * Next State: Has a hard state transition happened
     */
    private boolean nextHard;

    /**
     * Next State: Is the state in transition
     */
    private boolean nextTransitioning;

    /**
     * Next State: Is the state flapping between ok and not ok, but never reaching a hard state
     */
    private boolean nextFlapping;

    /**
     * Next State: When was the last hard state change
     */
    private long nextLastStateChange;

    /**
     * Next State: A bitmap of the ok history
     */
    private long nextOkHistory = 0x1L;

    /**
     * Next State: Was the last hard state ok?
     */
    private boolean nextLastHardOk = true;

    /**
     * Next State: What was the last hard status?
     */
    private String nextLastHardStatus;

    /**
     * Next State: What was the output of the last hard state
     */
    private String nextLastHardOutput;
    
    /**
     * Does this transition result in an alert, IE: A hard change from ok to not ok.
     */
    private boolean alert;
    
    /**
     * Does this transition result in a recovery, IE: A hard change from not ok to ok.
     */
    private boolean recovery;

    /**
     * Was this check previously in downtime
     */
    private boolean previousInDowntime;

    /**
     * Is this check now in downtime
     */
    private boolean nextInDowntime;
    
    /**
     * Was this check previously suppressed
     */
    private boolean previousSuppressed;

    /**
     * Is this check now suppressed
     */
    private boolean nextSuppressed;
    
    public CheckTransitionMO()
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

    public UUID getCheckId()
    {
        return checkId;
    }

    public void setCheckId(UUID checkId)
    {
        this.checkId = checkId;
    }

    public long getAppliedAt()
    {
        return appliedAt;
    }

    public void setAppliedAt(long appliedAt)
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

    public String getPreviousStatus()
    {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus)
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

    public long getPreviousLastCheckTime()
    {
        return previousLastCheckTime;
    }

    public void setPreviousLastCheckTime(long previousLastCheckTime)
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

    public long getPreviousLastStateChange()
    {
        return previousLastStateChange;
    }

    public void setPreviousLastStateChange(long previousLastStateChange)
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

    public String getPreviousLastHardStatus()
    {
        return previousLastHardStatus;
    }

    public void setPreviousLastHardStatus(String previousLastHardStatus)
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

    public String getNextStatus()
    {
        return nextStatus;
    }

    public void setNextStatus(String nextStatus)
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

    public long getNextLastCheckTime()
    {
        return nextLastCheckTime;
    }

    public void setNextLastCheckTime(long nextLastCheckTime)
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

    public long getNextLastStateChange()
    {
        return nextLastStateChange;
    }

    public void setNextLastStateChange(long nextLastStateChange)
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

    public String getNextLastHardStatus()
    {
        return nextLastHardStatus;
    }

    public void setNextLastHardStatus(String nextLastHardStatus)
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
}
