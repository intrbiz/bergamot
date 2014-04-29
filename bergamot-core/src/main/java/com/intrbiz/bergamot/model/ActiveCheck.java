package com.intrbiz.bergamot.model;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.intrbiz.bergamot.model.message.ActiveCheckMO;
import com.intrbiz.bergamot.model.message.task.check.ExecuteCheck;
import com.intrbiz.bergamot.model.util.Parameter;
import com.intrbiz.bergamot.util.RandStatus;

/**
 * A check which is actively polled
 */
public abstract class ActiveCheck extends Check
{
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

    public ActiveCheck()
    {
        super();
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

    public CheckCommand getCheckCommand()
    {
        return checkCommand;
    }

    public void setCheckCommand(CheckCommand checkCommand)
    {
        this.checkCommand = checkCommand;
    }

    public long getCurrentInterval()
    {
        return (this.getState().isOk() && this.getState().isHard()) ? this.getCheckInterval() : this.getRetryInterval();
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
    
    protected void toMO(ActiveCheckMO mo)
    {
        super.toMO(mo);
        mo.setCheckInterval(this.getCheckInterval());
        mo.setRetryInterval(this.getRetryInterval());
    }
}
