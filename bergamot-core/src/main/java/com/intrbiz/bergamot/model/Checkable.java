package com.intrbiz.bergamot.model;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.task.Check;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.util.RandStatus;

/**
 * An object which is checked
 */
public abstract class Checkable extends NamedObject
{
    protected int maxCheckAttempts = 3;

    protected long checkInterval = TimeUnit.MINUTES.toMillis(5);

    protected long retryInterval = TimeUnit.MINUTES.toMillis(1);

    protected CommandExecution commandExecution;

    protected CheckState state = new CheckState();

    protected TimePeriod checkPeriod;

    public Checkable()
    {
        super();
    }

    public abstract String getType();

    public int getMaxCheckAttempts()
    {
        return maxCheckAttempts;
    }

    public void setMaxCheckAttempts(int maxCheckAttempts)
    {
        this.maxCheckAttempts = maxCheckAttempts;
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
        check.setEngine("nagios");
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
