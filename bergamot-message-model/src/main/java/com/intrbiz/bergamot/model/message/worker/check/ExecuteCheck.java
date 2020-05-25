package com.intrbiz.bergamot.model.message.worker.check;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Execute this check please
 */
@JsonTypeName("bergamot.worker.check.execute")
public class ExecuteCheck extends CheckMessage
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("timeout")
    private long timeout = 30_000L;

    @JsonProperty("scheduled")
    private long scheduled;
    
    @JsonProperty("script")
    private String script;
    
    @JsonProperty("saved_state")
    private String savedState;

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

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }

    public String getSavedState()
    {
        return savedState;
    }

    public void setSavedState(String savedState)
    {
        this.savedState = savedState;
    }
}
