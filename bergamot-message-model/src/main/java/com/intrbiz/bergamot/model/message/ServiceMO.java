package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.service")
public class ServiceMO extends ActiveCheckMO
{
    @JsonProperty("host")
    private HostMO host;
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("application")
    private String application;
    
    public ServiceMO()
    {
        super();
    }
    
    public String getCheckType()
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
    
    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getApplication()
    {
        return application;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }
}
