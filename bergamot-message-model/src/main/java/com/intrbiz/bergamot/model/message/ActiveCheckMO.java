package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ActiveCheckMO extends CheckMO
{
    @JsonProperty("check_interval")
    protected long checkInterval;

    @JsonProperty("retry_interval")
    protected long retryInterval;

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
}
