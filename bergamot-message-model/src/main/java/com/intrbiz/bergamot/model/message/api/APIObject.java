package com.intrbiz.bergamot.model.message.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public class APIObject
{
    public APIObject()
    {
        super();
    }
}
