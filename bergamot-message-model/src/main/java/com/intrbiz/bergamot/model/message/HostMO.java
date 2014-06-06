package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("bergamot.host")
public class HostMO extends ActiveCheckMO
{
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("location")
    private LocationMO location;
    
    @JsonProperty("services")
    private List<ServiceMO> services = new LinkedList<ServiceMO>();
    
    @JsonProperty("traps")
    private List<TrapMO> traps = new LinkedList<TrapMO>();
    
    public HostMO()
    {
        super();
    }
    
    public String getCheckType()
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

    public List<ServiceMO> getServices()
    {
        return services;
    }

    public void setServices(List<ServiceMO> services)
    {
        this.services = services;
    }

    public LocationMO getLocation()
    {
        return location;
    }

    public void setLocation(LocationMO location)
    {
        this.location = location;
    }

    public List<TrapMO> getTraps()
    {
        return traps;
    }

    public void setTraps(List<TrapMO> traps)
    {
        this.traps = traps;
    }
}
