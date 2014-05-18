package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class RealCheckMO extends CheckMO
{
    @JsonProperty("alert_attempt_threshold")
    protected int alertAttemptThreshold;

    @JsonProperty("recovery_attempt_threshold")
    protected int recoveryAttemptThreshold;

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
}
