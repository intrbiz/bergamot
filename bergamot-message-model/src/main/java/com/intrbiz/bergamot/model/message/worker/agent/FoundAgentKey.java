package com.intrbiz.bergamot.model.message.worker.agent;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.SiteMO;
import com.intrbiz.bergamot.model.message.processor.agent.LookupAgentKey;

/**
 * Found an agent key
 */
@JsonTypeName("bergamot.worker.agent.key")
public class FoundAgentKey extends WorkerAgentMessage
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("key_id")
    private UUID keyId;
    
    @JsonProperty("key")
    private String key;
    
    public FoundAgentKey()
    {
        super();
    }
    
    public FoundAgentKey(LookupAgentKey replyTo)
    {
        super(replyTo);
        this.setWorkerId(replyTo.getWorkerId());
        this.keyId = replyTo.getKeyId();
    }

    public FoundAgentKey(LookupAgentKey replyTo, String key)
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
    
    @Override
    public UUID getSiteId()
    {
        return SiteMO.getSiteId(this.keyId);
    }
}
