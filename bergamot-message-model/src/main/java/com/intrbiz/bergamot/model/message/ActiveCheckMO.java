package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ActiveCheckMO extends RealCheckMO
{
    @JsonProperty("check_interval")
    protected long checkInterval;

    @JsonProperty("retry_interval")
    protected long retryInterval;
    
    @JsonProperty("current_interval")
    protected long currentInterval;
    
    @JsonProperty("command")
    protected CommandMO command;
    
    @JsonProperty("time_period")
    protected TimePeriodMO timePeriod;

    public ActiveCheckMO()
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

    public long getCurrentInterval()
    {
        return currentInterval;
    }

    public void setCurrentInterval(long currentInterval)
    {
        this.currentInterval = currentInterval;
    }

    public CommandMO getCommand()
    {
        return command;
    }

    public void setCommand(CommandMO command)
    {
        this.command = command;
    }

    public TimePeriodMO getTimePeriod()
    {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriodMO timePeriod)
    {
        this.timePeriod = timePeriod;
    }
}
