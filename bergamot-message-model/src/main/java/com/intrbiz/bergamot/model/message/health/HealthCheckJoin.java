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
    
    @JsonProperty("daemon-name")
    private String daemonName;
    
    @JsonProperty("started")
    private long started;
    
    @JsonProperty("host-name")
    private String hostName;
    
    public HealthCheckJoin()
    {
        super();
    }
    
    public HealthCheckJoin(UUID instanceId, String daemonName, long started, String hostName)
    {
        super();
        this.instanceId = instanceId;
        this.daemonName = daemonName;
        this.started = started;
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
}
