package com.intrbiz.bergamot.model.message.health;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Announce a daemons intention to start heartbeating and join the cluster
 */
@JsonTypeName("bergamot.healthcheck.join")
public class HealthCheckJoin extends HealthCheckMessage
{
    @JsonProperty("instance-id")
    private UUID instanceId;
    
    @JsonProperty("runtime-id")
    private UUID runtimeId;
    
    @JsonProperty("daemon-name")
    private String daemonName;
    
    @JsonProperty("started")
    private long started;
    
    @JsonProperty("host-id")
    private UUID hostId;
    
    @JsonProperty("host-name")
    private String hostName;
    
    public HealthCheckJoin()
    {
        super();
    }
    
    public HealthCheckJoin(UUID instanceId, UUID runtimeId, String daemonName, long started, UUID hostId, String hostName)
    {
        super();
        this.instanceId = instanceId;
        this.runtimeId = runtimeId;
        this.daemonName = daemonName;
        this.started = started;
        this.hostId = hostId;
        this.hostName = hostName;
    }

    public UUID getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId)
    {
        this.instanceId = instanceId;
    }

    public UUID getRuntimeId()
    {
        return runtimeId;
    }

    public void setRuntimeId(UUID runtimeId)
    {
        this.runtimeId = runtimeId;
    }

    public String getDaemonName()
    {
        return daemonName;
    }

    public void setDaemonName(String daemonName)
    {
        this.daemonName = daemonName;
    }

    public long getStarted()
    {
        return started;
    }

    public void setStarted(long started)
    {
        this.started = started;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public UUID getHostId()
    {
        return hostId;
    }

    public void setHostId(UUID hostId)
    {
        this.hostId = hostId;
    }
}
