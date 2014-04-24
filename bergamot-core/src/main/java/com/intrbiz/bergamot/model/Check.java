package com.intrbiz.bergamot.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intrbiz.bergamot.model.message.task.ExecuteCheck;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.util.RandStatus;

/**
 * An something which should be checked
 */
public abstract class Check extends NamedObject
{
    /**
     * How many check attempts to trigger a hard alert
     */
    protected int alertAttemptThreshold = 3;

    /**
     * How many check attempts to trigger a hard recovery
     */
    protected int recoveryAttemptThreshold = 3;

    /**
     * How often should checks be executed (in milliseconds)
     */
    protected long checkInterval = TimeUnit.MINUTES.toMillis(5);

    /**
     * How often should checks be executed when not in an ok state (in milliseconds)
     */
    protected long retryInterval = TimeUnit.MINUTES.toMillis(1);

    /**
     * The check command to execute
     */
    protected CheckCommand checkCommand;

    /**
     * The state of this check
     */
    protected CheckState state = new CheckState();

    /**
     * When should we check, a calendar
     */
    protected TimePeriod checkPeriod;

    /**
     * Is the result of this check suppressed
     */
    protected boolean suppressed = false;

    /**
     * Is this check currently scheduled
     */
    protected boolean enabled = true;

    /**
     * Checks which reference this check
     */
    private Set<Check> referencedBy = new HashSet<Check>();

    /**
     * Checks which this check references
     */
    private Set<Check> references = new HashSet<Check>();

    public Check()
    {
        super();
    }

    public abstract String getType();

    protected void onSetId()
    {
        this.state.setCheckId(this.id);
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

    public long getCurrentInterval()
    {
        return (this.getState().isOk() && this.getState().isHard()) ? this.getCheckInterval() : this.getRetryInterval();
    }

    public TimePeriod getCheckPeriod()
    {
        return checkPeriod;
    }

    public void setCheckPeriod(TimePeriod timePeriod)
    {
        this.checkPeriod = timePeriod;
    }

    public CheckCommand getCheckCommand()
    {
        return checkCommand;
    }

    public void setCheckCommand(CheckCommand checkCommand)
    {
        this.checkCommand = checkCommand;
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

    public Set<Check> getReferencedBy()
    {
        return referencedBy;
    }

    public void setReferencedBy(Set<Check> referencedBy)
    {
        this.referencedBy = referencedBy;
    }

    public Set<Check> getReferences()
    {
        return references;
    }

    public void setReferences(Set<Check> references)
    {
        this.references = references;
    }

    public CheckState getState()
    {
        return this.state;
    }

    public final ExecuteCheck createExecuteCheck()
    {
        if (this.checkCommand == null) return null;
        ExecuteCheck executeCheck = new ExecuteCheck();
        executeCheck.setId(UUID.randomUUID());
        executeCheck.setCheckType(this.getType());
        executeCheck.setCheckId(this.getId());
        executeCheck.setEngine(this.checkCommand.getCommand().getEngine());
        executeCheck.setName(this.checkCommand.getCommand().getName());
        // intrinsic check parameters
        this.setCheckParameters(executeCheck);
        // parameters defined by the command
        for (Parameter parameter : this.checkCommand.getCommand().getParameters())
        {
            executeCheck.addParameter(parameter.getName(), parameter.getValue());
        }
        // configured parameters
        for (Parameter parameter : this.checkCommand.getParameters())
        {
            executeCheck.setParameter(parameter.getName(), parameter.getValue());
        }
        executeCheck.setTimeout(30_000L);
        executeCheck.setScheduled(System.currentTimeMillis());
        return executeCheck;
    }

    protected void setCheckParameters(ExecuteCheck executeCheck)
    {
        executeCheck.addParameter("RANDSTATUS", String.valueOf(RandStatus.getInstance().randomNagiosStatus()));
    }
}
