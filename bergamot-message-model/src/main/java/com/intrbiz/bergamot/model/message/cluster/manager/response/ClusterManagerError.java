package com.intrbiz.bergamot.model.message.cluster.manager.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.cluster.manager.ClusterManagerResponse;

@JsonTypeName("bergamot.cluster.manager.error")
public class ClusterManagerError extends ClusterManagerResponse
{
    @JsonProperty("message")
    private String message;
    
    public ClusterManagerError()
    {
        super();
    }
    
    public ClusterManagerError(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
