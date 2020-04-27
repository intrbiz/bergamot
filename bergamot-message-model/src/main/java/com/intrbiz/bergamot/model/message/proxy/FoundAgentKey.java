package com.intrbiz.bergamot.model.message.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Found an agent key
 */
@JsonTypeName("bergamot.proxy.found.agent.key")
public class FoundAgentKey extends ProxyMessage
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("key")
    private String key;
    
    public FoundAgentKey()
    {
        super();
    }

    public FoundAgentKey(LookupAgentKey replyTo, String key)
    {
        super(replyTo);
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
}
