package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class MessageObject
{
    public MessageObject()
    {
        super();
    }
}
