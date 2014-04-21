package com.intrbiz.bergamot.model.task;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intrbiz.bergamot.model.message.Message;

/**
 * A message requiring work to be performed
 */
public abstract class Task extends Message
{
    @JsonProperty("id")
    private UUID id;
    
    public Task()
    {
        super();
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }
}
