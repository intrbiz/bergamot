package com.intrbiz.bergamot.model.message.processor.agent;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.SiteMO;
import com.intrbiz.bergamot.model.message.processor.ProcessorHashable;

/**
 * Lookup agent key
 */
@JsonTypeName("bergamot.processor.agent.key.lookup")
public class LookupAgentKey extends ProcessorAgentMessage implements ProcessorHashable
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("key_id")
    private UUID keyId;

    @JsonProperty("worker_id")
    private UUID workerId;

    public LookupAgentKey()
    {
        super();
    }

    public LookupAgentKey(UUID keyId, UUID workerId)
    {
        super();
        this.keyId = Objects.requireNonNull(keyId);
        this.workerId = Objects.requireNonNull(workerId);
        this.setSiteId(SiteMO.getSiteId(this.keyId));
    }

    public UUID getKeyId()
    {
        return this.keyId;
    }

    public void setKeyId(UUID keyId)
    {
        this.keyId = keyId;
        this.setSiteId(keyId == null ? null : SiteMO.getSiteId(keyId));
    }

    public UUID getWorkerId()
    {
        return this.workerId;
    }

    public void setWorkerId(UUID workerId)
    {
        this.workerId = workerId;
    }

    @Override
    public long routeHash()
    {
        return this.keyId.getLeastSignificantBits();
    }
}
