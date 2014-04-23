package com.intrbiz.bergamot.model;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intrbiz.bergamot.model.message.task.Check;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.util.RandStatus;

/**
 * An object which is checked
 */
public abstract class Checkable extends NamedObject
{
    protected int alertAttemptThreshold = 3;

    protected int recoveryAttemptThreshold = 3;

    protected long checkInterval = TimeUnit.MINUTES.toMillis(5);

    protected long retryInterval = TimeUnit.MINUTES.toMillis(1);

    protected CommandExecution commandExecution;

    protected CheckState state = new CheckState();

    protected TimePeriod checkPeriod;

    /**
     * Is the result of this check supressed
     */
    protected boolean supressed = false;

    /**
     * Is this check currently scheduled
     */
    protected boolean enabled = true;

    public Checkable()
    {
        super();
    }

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

    public int getCurrentAttemptThreshold()
    {
        return this.getState().isOk() ? this.getAlertAttemptThreshold() : this.getRecoveryAttemptThreshold();
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
        return this.getState().isOk() ? this.getCheckInterval() : this.getRetryInterval();
    }

    public TimePeriod getCheckPeriod()
    {
        return checkPeriod;
    }

    public void setCheckPeriod(TimePeriod timePeriod)
    {
        this.checkPeriod = timePeriod;
    }

    public CommandExecution getCommandExecution()
    {
        return commandExecution;
    }

    public void setCommandExecution(CommandExecution commandExecution)
    {
        this.commandExecution = commandExecution;
    }

    public boolean isSupressed()
    {
        return supressed;
    }

    public void setSupressed(boolean supressed)
    {
        this.supressed = supressed;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public CheckState getState()
    {
        return this.state;
    }

    public final Check createCheck()
    {
        if (this.commandExecution == null) return null;
        Check check = new Check();
        check.setId(UUID.randomUUID());
        check.setCheckableType(this.getType());
        check.setCheckableId(this.getId());
        check.setEngine(this.commandExecution.getCommand().getEngine());
        check.setName(this.commandExecution.getCommand().getName());
        // intrinsic check parameters
        this.setCheckParameters(check);
        // parameters defined by the command
        for (Parameter parameter : this.commandExecution.getCommand().getParameters())
        {
            check.addParameter(parameter.getName(), parameter.getValue());
        }
        // configured parameters
        for (Parameter parameter : this.commandExecution.getParameters())
        {
            check.setParameter(parameter.getName(), parameter.getValue());
        }
        check.setTimeout(30_000L);
        check.setScheduled(System.currentTimeMillis());
        return check;
    }

    protected void setCheckParameters(Check check)
    {
        check.addParameter("RANDSTATUS", String.valueOf(RandStatus.getInstance().randomNagiosStatus()));
    }
}
