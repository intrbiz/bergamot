package com.intrbiz.bergamot.model.message;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A message which will be published to a queue
 */
public abstract class Message extends MessageObject
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("id")
    private UUID id = UUID.randomUUID();
    
    @JsonProperty("reply_to")
    private UUID replyTo;

    public Message()
    {
        super();
    }
    
    public Message(UUID replyTo)
    {
        super();
        this.replyTo = replyTo;
    }
    
    public Message(Message replyTo)
    {
        this(replyTo.getId());
    }

    public final UUID getId()
    {
        return id;
    }

    public final void setId(UUID id)
    {
        this.id = id;
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
