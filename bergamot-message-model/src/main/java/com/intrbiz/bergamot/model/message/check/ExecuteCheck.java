package com.intrbiz.bergamot.model.message.check;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.event.check.CheckEvent;

/**
 * Execute this check please
 */
@JsonTypeName("bergamot.execute_check")
public class ExecuteCheck extends CheckEvent
{
    @JsonProperty("timeout")
    private long timeout = 30_000L;

    @JsonProperty("scheduled")
    private long scheduled;

    public ExecuteCheck()
    {
        super();
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getScheduled()
    {
        return scheduled;
    }

    public void setScheduled(long scheduled)
    {
        this.scheduled = scheduled;
    }
}
