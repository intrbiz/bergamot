package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.model.message.RealCheckMO;

/**
 * A 'real' check has to know about hard vs soft states
 */
public abstract class RealCheck extends Check
{
    /**
     * How many check attempts to trigger a hard alert
     */
    protected int alertAttemptThreshold = 3;

    /**
     * How many check attempts to trigger a hard recovery
     */
    protected int recoveryAttemptThreshold = 3;

    public RealCheck()
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
        return (this.getState().isOk() && this.getState().isHard()) ? this.getAlertAttemptThreshold() : this.getRecoveryAttemptThreshold();
    }
    
    protected void toMO(RealCheckMO mo)
    {
        super.toMO(mo);
        mo.setAlertAttemptThreshold(this.getAlertAttemptThreshold());
        mo.setRecoveryAttemptThreshold(this.getRecoveryAttemptThreshold());
    }
}
