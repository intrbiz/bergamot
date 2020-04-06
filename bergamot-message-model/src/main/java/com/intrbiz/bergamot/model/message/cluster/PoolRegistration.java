package com.intrbiz.bergamot.model.message.cluster;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.MessageObject;

/**
 * Registration information for a processing pool.
 */
@JsonTypeName("bergamot.cluster.registry.pool")
public class PoolRegistration extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    /**
     * The id of the agent
     */
    @JsonProperty("pool")
    private int pool;
    
    /**
     * When this worker registered
     */
    @JsonProperty("registered")
    private long registered;
    
    /**
     * The processor this pool has been assigned too
     */
    @JsonProperty("processor_id")
    private UUID processorId;
    
    public PoolRegistration()
    {
        super();
    }
    
    public PoolRegistration(int pool, long registered, UUID processorId)
    {
        super();
        this.pool = pool;
        this.registered = registered;
        this.processorId = processorId;
    }

    public int getPool()
    {
        return this.pool;
    }

    public void setPool(int pool)
    {
        this.pool = pool;
    }

    public long getRegistered()
    {
        return this.registered;
    }

    public void setRegistered(long registered)
    {
        this.registered = registered;
    }

    public UUID getProcessorId()
    {
        return this.processorId;
    }

    public void setProcessorId(UUID processorId)
    {
        this.processorId = processorId;
    }
}
