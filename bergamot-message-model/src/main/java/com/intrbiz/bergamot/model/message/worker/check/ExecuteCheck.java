package com.intrbiz.bergamot.model.message.worker.check;

import java.util.UUID;

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
    
    @JsonProperty("received")
    private long received;
    
    @JsonProperty("script")
    private String script;
    
    @JsonProperty("saved_state")
    private String savedState;
    
    /**
     * An id added to adhoc checks to correlate them with with the originator. This must be null for normal check executions
     */
    @JsonProperty("adhoc_id")
    private UUID adhocId;

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

    public long getReceived()
    {
        return this.received;
    }

    public void setReceived(long received)
    {
        this.received = received;
    }
    
    public UUID getAdhocId()
    {
        return adhocId;
    }

    public void setAdhocId(UUID adhocId)
    {
        this.adhocId = adhocId;
    }
}
