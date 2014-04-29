package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.state.CheckStateMO;

public abstract class CheckMO extends NamedObjectMO
{
    @JsonProperty("alert_attempt_threshold")
    protected int alertAttemptThreshold;

    @JsonProperty("recovery_attempt_threshold")
    protected int recoveryAttemptThreshold;

    @JsonProperty("check_interval")
    protected long checkInterval;

    @JsonProperty("retry_interval")
    protected long retryInterval;

    @JsonProperty("state")
    protected CheckStateMO state;

    @JsonProperty("suppressed")
    protected boolean suppressed;

    @JsonProperty("enabled")
    protected boolean enabled;

    public CheckMO()
    {
        super();
    }
    
    @JsonIgnore
    public abstract String getType();

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

    public long getCheckInterval()
    {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval)
    {
        this.checkInterval = checkInterval;
    }

    public long getRetryInterval()
    {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval)
    {
        this.retryInterval = retryInterval;
    }

    public CheckStateMO getState()
    {
        return state;
    }

    public void setState(CheckStateMO state)
    {
        this.state = state;
    }

    public boolean isSuppressed()
    {
        return suppressed;
    }

    public void setSuppressed(boolean suppressed)
    {
        this.suppressed = suppressed;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
