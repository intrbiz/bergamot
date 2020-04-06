package com.intrbiz.bergamot.model.message.cluster;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * Registration information for an agent.
 * 
 */
@JsonTypeName("bergamot.cluster.registry.agent")
public class AgentRegistration extends MessageObject implements Comparable<AgentRegistration>
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The id of the agent
     */
    @JsonProperty("id")
    private UUID id;
    
    /**
     * When this worker registered
     */
    @JsonProperty("registered")
    private long registered;
    
    /**
     * The id of the worker that the agent is connected to
     */
    @JsonProperty("worker_id")
    private UUID workerId;
    
    public AgentRegistration()
    {
        super();
    }
    
    public AgentRegistration(UUID id, long registered, UUID workerId)
    {
        super();
        this.id = Objects.requireNonNull(id);
        this.registered = registered;
        this.workerId = workerId;
    }

    public UUID getId()
    {
        return id;
    }

    public long getRegistered()
    {
        return this.registered;
    }

    public void setRegistered(long registered)
    {
        this.registered = registered;
    }

    public UUID getWorkerId()
    {
        return this.workerId;
    }

    public void setWorkerId(UUID workerId)
    {
        this.workerId = workerId;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        AgentRegistration other = (AgentRegistration) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int compareTo(AgentRegistration o)
    {
        return this.id.compareTo(o.id);
    }
}
