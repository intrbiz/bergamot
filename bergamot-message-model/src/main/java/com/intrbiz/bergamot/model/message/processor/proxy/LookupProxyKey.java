package com.intrbiz.bergamot.model.message.processor.proxy;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.processor.ProcessorHashable;

/**
 * Lookup agent key
 */
@JsonTypeName("bergamot.processor.proxy.key.lookup")
public class LookupProxyKey extends ProcessorProxyMessage implements ProcessorHashable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("key_id")
    private UUID keyId;

    @JsonProperty("proxy_id")
    private UUID proxyId;

    public LookupProxyKey()
    {
        super();
    }

    public LookupProxyKey(UUID keyId, UUID proxyId)
    {
        super();
        this.keyId = Objects.requireNonNull(keyId);
        this.proxyId = Objects.requireNonNull(proxyId);
    }

    public UUID getKeyId()
    {
        return this.keyId;
    }

    public void setKeyId(UUID keyId)
    {
        this.keyId = keyId;
    }

    public UUID getProxyId()
    {
        return this.proxyId;
    }

    public void setProxyId(UUID proxyId)
    {
        this.proxyId = proxyId;
    }

    @Override
    public long routeHash()
    {
        return this.keyId.getLeastSignificantBits();
    }
}
