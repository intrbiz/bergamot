package com.intrbiz.bergamot.model.message.health;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Send a heartbeat
 */
@JsonTypeName("bergamot.healthcheck.heartbeat")
public class HealthCheckHeartbeat extends HealthCheckMessage
{
    @JsonProperty("instance-id")
    private UUID instanceId;
    
    @JsonProperty("time")
    private long time;
    
    @JsonProperty("sequence")
    private long sequence;
    
    public HealthCheckHeartbeat()
    {
        super();
    }
    
    public HealthCheckHeartbeat(UUID instanceId, long time, long sequence)
    {
        super();
        this.instanceId = instanceId;
        this.time = time;
        this.sequence = sequence;
    }

    public UUID getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId)
    {
        this.instanceId = instanceId;
    }

    public long getTime()
    {
        return time;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public long getSequence()
    {
        return sequence;
    }

    public void setSequence(long sequence)
    {
        this.sequence = sequence;
    }
}
