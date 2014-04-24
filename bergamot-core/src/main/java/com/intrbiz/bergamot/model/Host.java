package com.intrbiz.bergamot.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.config.model.HostCfg;
import com.intrbiz.bergamot.model.message.task.ExecuteCheck;
import com.intrbiz.bergamot.model.state.GroupState;

/**
 * A host - some form of network connected device that is to be checked
 */
public class Host extends Check
{
    private String address;

    private Map<String, Service> services = new TreeMap<String, Service>();

    private Set<HostGroup> hostGroups = new HashSet<HostGroup>();

    /**
     * The location of this host
     */
    private Location location;

    public Host()
    {
        super();
    }

    public final String getType()
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

    public void configure(HostCfg config)
    {
        this.name = config.resolveHostName();
        this.address = Util.coalesceEmpty(config.resolveAddress(), this.name);
        this.displayName = Util.coalesceEmpty(config.resolveDisplayName(), config.resolveAlias(), this.name);
        this.alertAttemptThreshold = config.resolveMaxCheckAttempts();
        this.recoveryAttemptThreshold = config.resolveMaxCheckAttempts();
        this.checkInterval = TimeUnit.MINUTES.toMillis(config.resolveCheckInterval());
        this.retryInterval = TimeUnit.MINUTES.toMillis(config.resolveRetryInterval());
    }

    public Set<String> getServiceNames()
    {
        return this.services.keySet();
    }

    public Collection<Service> getServices()
    {
        return this.services.values();
    }

    public void addService(Service service)
    {
        this.services.put(service.getName(), service);
        service.setHost(this);
    }

    public Service getService(String name)
    {
        return this.services.get(name);
    }

    public boolean containsService(String name)
    {
        return this.services.containsKey(name);
    }

    public int getServiceCount()
    {
        return this.services.size();
    }

    public Set<HostGroup> getHostgroups()
    {
        return hostGroups;
    }

    public void addHostgroup(HostGroup hostGroup)
    {
        this.hostGroups.add(hostGroup);
    }

    protected void setCheckParameters(ExecuteCheck executeCheck)
    {
        super.setCheckParameters(executeCheck);
        // intrinsic parameters
        executeCheck.addParameter("HOSTADDRESS", this.getAddress());
        executeCheck.addParameter("HOSTNAME", this.getName());
    }

    public GroupState getServicesState()
    {
        return GroupState.compute(this.getServices());
    }

    public Location getLocation()
    {
        return location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public String toString()
    {
        return "Host (" + this.id + ") " + this.name + " check " + this.checkCommand;
    }
}
