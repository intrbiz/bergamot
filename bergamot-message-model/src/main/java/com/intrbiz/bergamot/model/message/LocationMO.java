package com.intrbiz.bergamot.model.message;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intrbiz.bergamot.model.message.state.GroupStateMO;

/**
 * Contact message object
 */
@JsonTypeName("bergamot.location")
public class LocationMO extends SecuredObjectMO
{
    @JsonProperty("state")
    private GroupStateMO state;

    @JsonProperty("location")
    private LocationMO location;

    @JsonProperty("children")
    private List<LocationMO> children = new LinkedList<LocationMO>();

    @JsonProperty("hosts")
    private List<HostMO> hosts = new LinkedList<HostMO>();

    public LocationMO()
    {
        super();
    }

    public List<HostMO> getHosts()
    {
        return hosts;
    }

    public void setHosts(List<HostMO> hosts)
    {
        this.hosts = hosts;
    }

    public LocationMO getLocation()
    {
        return location;
    }

    public void setLocation(LocationMO location)
    {
        this.location = location;
    }

    public List<LocationMO> getChildren()
    {
        return children;
    }

    public void setChildren(List<LocationMO> children)
    {
        this.children = children;
    }

    public GroupStateMO getState()
    {
        return state;
    }

    public void setState(GroupStateMO state)
    {
        this.state = state;
    }
}
