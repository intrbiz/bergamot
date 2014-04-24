package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.config.model.LocationCfg;
import com.intrbiz.bergamot.model.state.GroupState;

/**
 * The physical (probably) location of a host
 */
public class Location extends NamedObject
{
    private Map<String, Host> hosts = new TreeMap<String, Host>();

    private Location location;

    private Map<String, Location> locations = new TreeMap<String, Location>();

    public Location()
    {
        super();
    }

    public void configure(LocationCfg config)
    {
        this.name = config.resolveLocationName();
        this.displayName = Util.coalesceEmpty(config.resolveAlias(), this.name);
    }

    public Collection<Host> getHosts()
    {
        return this.hosts.values();
    }

    public Host getHost(String name)
    {
        return this.hosts.get(name);
    }

    public boolean containsHost(String name)
    {
        return this.hosts.containsKey(name);
    }

    public int getHostCount()
    {
        return this.hosts.size();
    }

    public void addHost(Host host)
    {
        this.hosts.put(host.getName(), host);
        host.setLocation(this);
    }

    public Collection<Location> getLocations()
    {
        return this.locations.values();
    }

    public Location getLocation(String name)
    {
        return this.locations.get(name);
    }

    public boolean containsLocation(String name)
    {
        return this.locations.containsKey(name);
    }

    public int getLocationCount()
    {
        return this.locations.size();
    }

    public void addLocation(Location location)
    {
        this.locations.put(location.getName(), location);
        location.setLocation(this);
    }

    public Location getLocation()
    {
        return location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public GroupState getState()
    {
        return GroupState.compute(this.getHosts());
    }
}
