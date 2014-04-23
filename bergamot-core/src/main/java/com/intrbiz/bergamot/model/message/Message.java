package com.intrbiz.bergamot.model.message;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A message which will be published to a queue
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class Message
{
    /**
     * Use the JsonTypeName annotation to lookup the given message type name
     */
    public static final String getMessageType(Class<? extends Message> cls)
    {
        JsonTypeName typeName = cls.getAnnotation(JsonTypeName.class);
        if (typeName == null) throw new RuntimeException("The message " + cls + " is not annotated with it's type name!");
        return typeName.value();
    }

    @JsonIgnore()
    private final String typeName = getMessageType(this.getClass());

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("sender")
    private String sender;

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

    public final String getSender()
    {
        return sender;
    }

    public final void setSender(String sender)
    {
        this.sender = sender;
    }

    /**
     * Get the type name for this message
     * 
     * @return
     */
    @JsonIgnore
    public final String getType()
    {
        return this.typeName;
    }

    /**
     * Get the default queue name for this message
     */
    @JsonIgnore
    public String getDefaultRoute()
    {
        return this.getType();
    }
}
