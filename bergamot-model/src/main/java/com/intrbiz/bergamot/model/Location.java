package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.bergamot.model.message.LocationMO;
import com.intrbiz.bergamot.model.state.GroupState;
import com.intrbiz.configuration.Configurable;

/**
 * The physical (probably) location of a host
 */
public class Location extends NamedObject<LocationMO> implements Configurable<LocationCfg>
{
    private Location location;
    
    private Map<String, Host> hosts = new TreeMap<String, Host>();

    private Map<String, Location> locations = new TreeMap<String, Location>();
    
    private LocationCfg config;

    public Location()
    {
        super();
    }
    
    @Override
    public LocationCfg getConfiguration()
    {
        return this.config;
    }

    @Override
    public void configure(LocationCfg config)
    {
        this.config = config;
        LocationCfg rcfg = config.resolve();
        this.name = rcfg.getName();
        this.summary = Util.coalesceEmpty(rcfg.getSummary(), this.name);
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
        return GroupState.compute(this.getHosts(), this.getLocations(), (l) -> { return l.getState(); });
    }
    
    @Override    
    public LocationMO toMO(boolean stub)
    {
        LocationMO mo = new LocationMO();
        super.toMO(mo, stub);        
        mo.setState(this.getState().toMO());
        if (! stub)
        {
            mo.setLocation(Util.nullable(this.location, Location::toStubMO));
            mo.setChildren(this.getLocations().stream().map(Location::toStubMO).collect(Collectors.toList()));
            mo.setHosts(this.getHosts().stream().map(Host::toStubMO).collect(Collectors.toList()));
        }
        return  mo;
    }
}
