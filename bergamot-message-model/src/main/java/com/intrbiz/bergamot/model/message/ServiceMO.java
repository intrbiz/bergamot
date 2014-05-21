package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.service")
public class ServiceMO extends ActiveCheckMO
{
    @JsonProperty("host")
    private HostMO host;
    
    public ServiceMO()
    {
        super();
    }
    
    public String getType()
    {
        return "service";
    }

    public HostMO getHost()
    {
        return host;
    }

    public void setHost(HostMO host)
    {
        this.host = host;
    }
}
