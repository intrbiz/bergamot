package com.intrbiz.bergamot.model.message.check;

import java.util.UUID;

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
    
    @JsonProperty("agent-id")
    private UUID agentId;
    
    @JsonProperty("script")
    private String script;

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

    public UUID getAgentId()
    {
        return agentId;
    }

    public void setAgentId(UUID agentId)
    {
        this.agentId = agentId;
    }

    public String getScript()
    {
        return script;
    }

    public void setScript(String script)
    {
        this.script = script;
    }
}
