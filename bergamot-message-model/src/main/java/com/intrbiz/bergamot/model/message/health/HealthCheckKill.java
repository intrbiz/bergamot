package com.intrbiz.bergamot.model.message.health;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Request that the receiving node immediately terminates execution
 */
@JsonTypeName("bergamot.healthcheck.kill")
public class HealthCheckKill extends HealthCheckMessage
{
    @JsonProperty("instance-id")
    private UUID instanceId;
    
    @JsonProperty("runtime-id")
    private UUID runtimeId;
    
    @JsonProperty("password")
    private String password;
    
    public HealthCheckKill()
    {
        super();
    }
    
    public HealthCheckKill(UUID instanceId, UUID runtimeId, String password)
    {
        super();
        this.instanceId = instanceId;
        this.runtimeId = runtimeId;
        this.password = password;
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

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
