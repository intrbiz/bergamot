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

    public Message()
    {
        super();
    }

    public final UUID getId()
    {
        return id;
    }

    public final void setId(UUID id)
    {
        this.id = id;
    }
}
