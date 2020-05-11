package com.intrbiz.bergamot.model.message.proxy;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.processor.proxy.LookupProxyKey;

/**
 * Found a proxy key
 */
@JsonTypeName("bergamot.proxy.key")
public class FoundProxyKey extends ProxyMessage
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("site_id")
    private UUID siteId;
    
    public FoundProxyKey()
    {
        super();
    }
    
    public FoundProxyKey(LookupProxyKey replyTo)
    {
        super(replyTo);
        this.setProxyId(replyTo.getProxyId());
    }

    public FoundProxyKey(LookupProxyKey replyTo, String key)
    {
        this(replyTo);
        this.key = key;
    }

    public String getKey()
    {
        return this.key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public UUID getSiteId()
    {
        return this.siteId;
    }

    public void setSiteId(UUID siteId)
    {
        this.siteId = siteId;
    }
}
