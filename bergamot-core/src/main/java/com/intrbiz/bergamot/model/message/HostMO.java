package com.intrbiz.bergamot.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.host")
public class HostMO extends ActiveCheckMO
{
    @JsonProperty("address")
    private String address;
    
    public HostMO()
    {
        super();
    }
    
    public String getType()
    {
        return "host";
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }
    
    public String toString()
    {
        return "host { id: " + this.id + "}";
    }
}
