package com.intrbiz.bergamot.store;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.HostGroup;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.ServiceGroup;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.util.RingBuffer;

/**
 * Default, in-memory store of Bergamot objects: host, services, etc
 */
public class ObjectStore
{
    private Map<String, Host> hosts = new TreeMap<String, Host>();
    
    private Map<UUID, Host> hostsById = new TreeMap<UUID, Host>();
    
    private Map<String, HostGroup> hostGroups = new TreeMap<String, HostGroup>();
    
    private Map<String, ServiceGroup> serviceGroups = new TreeMap<String, ServiceGroup>();
    
    private Map<String, Command> commands = new TreeMap<String, Command>();
    
    private Map<UUID, Service> services = new TreeMap<UUID, Service>();
    
    private Map<String, TimePeriod> timePeriods = new TreeMap<String, TimePeriod>();
    
    private RingBuffer<Check> recentChecks = new RingBuffer<Check>(12); 

    public ObjectStore()
    {
        super();
    }
    
    // hosts

    public Host lookupHost(String host)
    {
        return this.hosts.get(host);
    }
    
    public Host lookupHost(UUID id)
    {
        return this.hostsById.get(id);
    }

    public void addHost(Host host)
    {
        this.hosts.put(host.getName(), host);
        this.hostsById.put(host.getId(), host);
    }

    public boolean containsHost(String host)
    {
        return this.hosts.containsKey(host);
    }

    public Collection<Host> getHosts()
    {
        return this.hosts.values();
    }
    
    // hostgroups
    
    public HostGroup lookupHostgroup(String name)
    {
        return this.hostGroups.get(name);
    }

    public void addHostgroup(HostGroup hostGroup)
    {
        this.hostGroups.put(hostGroup.getName(), hostGroup);
    }

    public boolean containsHostgroup(String hostgroup)
    {
        return this.hostGroups.containsKey(hostgroup);
    }

    public Collection<HostGroup> getHostgroups()
    {
        return this.hostGroups.values();
    }
    
    // servicegroups
    
    public ServiceGroup lookupServicegroup(String name)
    {
        return this.serviceGroups.get(name);
    }

    public void addServicegroup(ServiceGroup serviceGroup)
    {
        this.serviceGroups.put(serviceGroup.getName(), serviceGroup);
    }

    public boolean containsServicegroup(String servicegroup)
    {
        return this.serviceGroups.containsKey(servicegroup);
    }

    public Collection<ServiceGroup> getServicegroups()
    {
        return this.serviceGroups.values();
    }
    
    // services
    
    public Service lookupService(UUID id)
    {
        return this.services.get(id);
    }

    public void addService(Service service)
    {
        this.services.put(service.getId(), service);
    }

    public boolean containsService(UUID id)
    {
        return this.services.containsKey(id);
    }

    public Collection<Service> getServices()
    {
        return this.services.values();
    }
    
    // command
    
    public Command lookupCommand(String engine, String name)
    {
        return this.commands.get(engine + "::" + name);
    }

    public void addCommand(Command command)
    {
        this.commands.put(command.getEngine() + "::" + command.getName(), command);
    }

    public boolean containsCommand(String engine, String command)
    {
        return this.commands.containsKey(engine + "::" + command);
    }

    public Collection<Command> getCommands()
    {
        return this.commands.values();
    }
    
    // checkable
    
    public Check lookupCheckable(String type, UUID id)
    {
        if ("service".equals(type))
            return this.lookupService(id);
        if ("host".equals(type))
            return this.lookupHost(id);
        return null;
    }
    
    // stats
    
    public int getHostCount()
    {
        return this.hosts.size();
    }
    
    public int getHostgroupCount()
    {
        return this.hostGroups.size();
    }
    
    public int getServiceCount()
    {
        return this.services.size();
    }
    
    public int getServicegroupCount()
    {
        return this.serviceGroups.size();
    }
    
    public int getCommandCount()
    {
        return this.commands.size();
    }
    
    public int getTimePeriodCount()
    {
        return this.timePeriods.size();
    }
    
    // recent
    
    public List<Check> getRecentChecks()
    {
        return this.recentChecks.toList();
    }
    
    public void addRecentCheck(Check check)
    {
        this.recentChecks.add(check);
    }
    
    public void removeRecentCheck(Check check)
    {
        this.recentChecks.remove(check);
    }
    
    // time period
    
    public TimePeriod lookupTimePeriod(String timePeriod)
    {
        return this.timePeriods.get(timePeriod);
    }

    public void addTimePeriod(TimePeriod timePeriod)
    {
        this.timePeriods.put(timePeriod.getName(), timePeriod);
    }

    public boolean containsTimePeriod(String timePeriod)
    {
        return this.timePeriods.containsKey(timePeriod);
    }

    public Collection<TimePeriod> getTimePeriods()
    {
        return this.timePeriods.values();
    }
}
