package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.state.CheckStatsMO;

public abstract class RealCheckMO extends CheckMO
{
    @JsonProperty("alert_attempt_threshold")
    protected int alertAttemptThreshold;

    @JsonProperty("recovery_attempt_threshold")
    protected int recoveryAttemptThreshold;
    
    @JsonProperty("current_attempt_threshold")
    protected int currentAttemptThreshold;
    
    @JsonProperty("check_command")
    protected CheckCommandMO checkCommand;
    
    @JsonProperty("stats")
    protected CheckStatsMO stats;
    
    @JsonProperty("depends")
    protected List<? extends CheckMO> depends = new LinkedList<CheckMO>();

    public RealCheckMO()
    {
        super();
    }

    public int getAlertAttemptThreshold()
    {
        return alertAttemptThreshold;
    }

    public void setAlertAttemptThreshold(int alertAttemptThreshold)
    {
        this.alertAttemptThreshold = alertAttemptThreshold;
    }

    public int getRecoveryAttemptThreshold()
    {
        return recoveryAttemptThreshold;
    }

    public void setRecoveryAttemptThreshold(int recoveryAttemptThreshold)
    {
        this.recoveryAttemptThreshold = recoveryAttemptThreshold;
    }

    public int getCurrentAttemptThreshold()
    {
        return currentAttemptThreshold;
    }

    public void setCurrentAttemptThreshold(int currentAttemptThreshold)
    {
        this.currentAttemptThreshold = currentAttemptThreshold;
    }

    public CheckCommandMO getCheckCommand()
    {
        return checkCommand;
    }

    public void setCheckCommand(CheckCommandMO checkCommand)
    {
        this.checkCommand = checkCommand;
    }

    public CheckStatsMO getStats()
    {
        return stats;
    }

    public void setStats(CheckStatsMO stats)
    {
        this.stats = stats;
    }

    public List<? extends CheckMO> getDepends()
    {
        return depends;
    }

    public void setDepends(List<? extends CheckMO> depends)
    {
        this.depends = depends;
    }
}
