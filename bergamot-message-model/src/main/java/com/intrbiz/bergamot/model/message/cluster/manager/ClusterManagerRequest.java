package com.intrbiz.bergamot.model.message.cluster.manager;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.intrbiz.bergamot.io.BergamotTranscoder;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class ClusterManagerRequest
{
    public final String toString()
    {
        return new BergamotTranscoder().encodeAsString(this);
    }
}
