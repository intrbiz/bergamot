package com.intrbiz.bergamot.model.message.proxy;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;

/**
 * A message sent to or from a proxy node
 */
public abstract class ProxyMessage extends Message
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("reply_to")
    private UUID replyTo;
    
    @JsonProperty("proxy_id")
    private UUID proxyId;

    public ProxyMessage()
    {
        super();
    }

    public ProxyMessage(UUID replyTo)
    {
        super(replyTo);
    }

    public ProxyMessage(Message replyTo)
    {
        super();
        this.replyTo = replyTo.getId();
    }

    public UUID getReplyTo()
    {
        return this.replyTo;
    }

    public void setReplyTo(UUID replyTo)
    {
        this.replyTo = replyTo;
    }

    public UUID getProxyId()
    {
        return this.proxyId;
    }

    public void setProxyId(UUID proxyId)
    {
        this.proxyId = proxyId;
    }
}
