package com.intrbiz.bergamot.model.message;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.io.BergamotTranscoder;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class MessageObject implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1L;

    /**
     * Use the JsonTypeName annotation to lookup the given message type name
     */
    public static final String getMessageType(Class<? extends MessageObject> cls)
    {
        JsonTypeName typeName = cls.getAnnotation(JsonTypeName.class);
        if (typeName == null) throw new RuntimeException("The message " + cls + " is not annotated with it's type name!");
        return typeName.value();
    }

    @JsonIgnore()
    private final String typeName = getMessageType(this.getClass());

    public MessageObject()
    {
        super();
    }
    
    @SuppressWarnings("unchecked")
    public <T extends MessageObject> T cloneMessge()
    {
        try
        {
            return (T) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
        }
        return null;
    }

    public final String toString()
    {
        return new BergamotTranscoder().encodeAsString(this);
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
}
