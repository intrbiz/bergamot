package com.intrbiz.bergamot.model.message.proxy;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Lookup agent key
 */
@JsonTypeName("bergamot.proxy.lookup.agent.key")
public class LookupAgentKey extends ProxyMessage
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("key_id")
    private UUID keyId;
    
    public LookupAgentKey()
    {
        super();
    }

    public LookupAgentKey(UUID keyId)
    {
        super();
        this.keyId = keyId;
    }

    public UUID getKeyId()
    {
        return this.keyId;
    }

    public void setKeyId(UUID keyId)
    {
        this.keyId = keyId;
    }
}
