package com.intrbiz.bergamot.model;

import com.intrbiz.bergamot.config.model.RealCheckCfg;
import com.intrbiz.bergamot.model.message.RealCheckMO;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.data.db.compiler.meta.SQLColumn;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

/**
 * A 'real' check has to know about hard vs soft states
 */
public abstract class RealCheck<T extends RealCheckMO, C extends RealCheckCfg<C>> extends Check<T, C>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * How many check attempts to trigger a hard alert
     */
    @SQLColumn(index = 1, name = "alert_attempt_threshold", since = @SQLVersion({ 1, 0, 0 }))
    protected int alertAttemptThreshold = 3;

    /**
     * How many check attempts to trigger a hard recovery
     */
    @SQLColumn(index = 2, name = "recovery_attempt_threshold", since = @SQLVersion({ 1, 0, 0 }))
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
        return this.computeCurrentAttemptThreshold(this.getState());
    }
    
    public int computeCurrentAttemptThreshold(CheckState currentState)
    {
        return currentState.isOk() ? this.getRecoveryAttemptThreshold() : this.getAlertAttemptThreshold();
    }
    
    protected void toMO(RealCheckMO mo, boolean stub)
    {
        super.toMO(mo, stub);
        mo.setAlertAttemptThreshold(this.getAlertAttemptThreshold());
        mo.setRecoveryAttemptThreshold(this.getRecoveryAttemptThreshold());
        mo.setCurrentAttemptThreshold(this.getCurrentAttemptThreshold());
    }
}
