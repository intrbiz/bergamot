package com.intrbiz.bergamot.model.message.proxy;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;


/**
 * A Bergamot Proxy message
 */
public abstract class ProxyMessage extends Message
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("reply_to")
    private UUID replyTo;

    public ProxyMessage()
    {
        super();
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
}
