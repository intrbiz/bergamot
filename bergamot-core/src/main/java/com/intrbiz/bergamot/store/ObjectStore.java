package com.intrbiz.bergamot.store;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.Trap;

/**
 * Default, in-memory store of Bergamot objects: host, services, etc
 */
public class ObjectStore
{
    private Map<String, Host> hosts = new TreeMap<String, Host>();
    
    private Map<UUID, Host> hostsById = new TreeMap<UUID, Host>();
    
    private Map<String, Group> groups = new TreeMap<String, Group>();
    
    private Map<String, Command> commands = new TreeMap<String, Command>();
    
    private Map<UUID, Service> services = new TreeMap<UUID, Service>();
    
    private Map<UUID, Trap> traps = new TreeMap<UUID, Trap>();
    
    private Map<String, TimePeriod> timePeriods = new TreeMap<String, TimePeriod>();
    
    private Set<Check<?>> alerts = new HashSet<Check<?>>();
    
    private Map<String, Location> locations = new TreeMap<String, Location>();
    
    private Map<String, Team> teams = new TreeMap<String, Team>();
    
    private Map<String, Contact> contacts = new TreeMap<String, Contact>();
    
    private Map<UUID, Resource> resources = new TreeMap<UUID, Resource>();
    
    private Map<String, Cluster> clusters = new TreeMap<String, Cluster>();
    
    private Map<UUID, Cluster> clustersById = new TreeMap<UUID, Cluster>();

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
    
    // groups
    
    public Group lookupGroup(String name)
    {
        return this.groups.get(name);
    }
    
    public Group lookupGroup(UUID id)
    {
        return this.groups.values().stream().filter((e)->{return id.equals(e.getId());}).findFirst().get();
    }

    public void addGroup(Group group)
    {
        this.groups.put(group.getName(), group);
    }

    public boolean containsGroup(String group)
    {
        return this.groups.containsKey(group);
    }

    public Collection<Group> getGroups()
    {
        return this.groups.values();
    }
    
    public Set<Group> getRootGroups()
    {
        return this.groups.values().stream().filter((e) -> {return e.getParents().isEmpty();}).collect(Collectors.toSet());
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
    
    public Command lookupCommand(String name)
    {
        return this.commands.get(name);
    }
    
    public Command lookupCommand(UUID id)
    {
        return this.commands.values().stream().filter((c)->{return id.equals(c.getId());}).findFirst().get();
    }

    public void addCommand(Command command)
    {
        this.commands.put(command.getName(), command);
    }

    public boolean containsCommand(String command)
    {
        return this.commands.containsKey(command);
    }

    public Collection<Command> getCommands()
    {
        return this.commands.values();
    }
    
    // checkable
    
    public Check<?> lookupCheckable(String type, UUID id)
    {
        if ("service".equals(type))
            return this.lookupService(id);
        if ("trap".equals(type))
            return this.lookupTrap(id);
        if ("host".equals(type))
            return this.lookupHost(id);
        return null;
    }
    
    // stats
    
    public int getHostCount()
    {
        return this.hosts.size();
    }
    
    public int getGroupCount()
    {
        return this.groups.size();
    }
    
    public int getServiceCount()
    {
        return this.services.size();
    }
    
    public int getTrapCount()
    {
        return this.services.size();
    }
    
    public int getCommandCount()
    {
        return this.commands.size();
    }
    
    public int getTimePeriodCount()
    {
        return this.timePeriods.size();
    }
    
    public int getLocationCount()
    {
        return this.locations.size();
    }
    
    public int getTeamCount()
    {
        return this.teams.size();
    }
    
    public int getContactsCount()
    {
        return this.contacts.size();
    }
    
    public int getResourcesCount()
    {
        return this.contacts.size();
    }
    
    public int getClustersCount()
    {
        return this.clusters.size();
    }
    
    // recent
    
    public Collection<Check<?>> getAlerts()
    {
        return this.alerts;
    }
    
    public void addAlert(Check<?> check)
    {
        this.alerts.add(check);
    }
    
    public void removeAlert(Check<?> check)
    {
        this.alerts.remove(check);
    }
    
    // time period
    
    public TimePeriod lookupTimePeriod(String timePeriod)
    {
        return this.timePeriods.get(timePeriod);
    }
    
    public TimePeriod lookupTimePeriod(UUID id)
    {
        return this.timePeriods.values().stream().filter((e)->{return id.equals(e.getId());}).findFirst().get();
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
    
    // location
    
    public Location lookupLocation(String location)
    {
        return this.locations.get(location);
    }
    
    public Location lookupLocation(UUID id)
    {
        return this.locations.values().stream().filter((e)->{return id.equals(e.getId());}).findFirst().get();
    }

    public void addLocation(Location location)
    {
        this.locations.put(location.getName(), location);
    }

    public boolean containsLocation(String location)
    {
        return this.locations.containsKey(location);
    }

    public Collection<Location> getLocations()
    {
        return this.locations.values();
    }
    
    // teams
    
    public Team lookupTeam(String contactGroup)
    {
        return this.teams.get(contactGroup);
    }
    
    public Team lookupTeam(UUID id)
    {
        return this.teams.values().stream().filter((e)->{return id.equals(e.getId());}).findFirst().get();
    }

    public void addTeam(Team team)
    {
        this.teams.put(team.getName(), team);
    }

    public boolean containsTeam(String contactGroup)
    {
        return this.teams.containsKey(contactGroup);
    }

    public Collection<Team> getTeams()
    {
        return this.teams.values();
    }
    
    // contacts
    
    public Contact lookupContact(String contact)
    {
        return this.contacts.get(contact);
    }
    
    public Contact lookupContact(UUID id)
    {
        return this.contacts.values().stream().filter((e)->{return id.equals(e.getId());}).findFirst().get();
    }
    
    public Contact lookupContactByEmail(String email)
    {
        return this.contacts.values().stream().filter((e)->{return email.equals(e.getEmail());}).findFirst().get();
    }

    public void addContact(Contact contact)
    {
        this.contacts.put(contact.getName(), contact);
    }

    public boolean containsContact(String contact)
    {
        return this.contacts.containsKey(contact);
    }

    public Collection<Contact> getContacts()
    {
        return this.contacts.values();
    }
    
    // resources
    
    public Resource lookupResource(UUID id)
    {
        return this.resources.get(id);
    }

    public void addResource(Resource resource)
    {
        this.resources.put(resource.getId(), resource);
    }

    public boolean containsResource(UUID id)
    {
        return this.resources.containsKey(id);
    }

    public Collection<Resource> getResources()
    {
        return this.resources.values();
    }
    
    // traps
    
    public Trap lookupTrap(UUID id)
    {
        return this.traps.get(id);
    }

    public void addTrap(Trap trap)
    {
        this.traps.put(trap.getId(), trap);
    }

    public boolean containsTrap(UUID id)
    {
        return this.traps.containsKey(id);
    }

    public Collection<Trap> getTraps()
    {
        return this.traps.values();
    }
    
    // clusters

    public Cluster lookupCluster(String name)
    {
        return this.clusters.get(name);
    }
    
    public Cluster lookupCluster(UUID id)
    {
        return this.clustersById.get(id);
    }

    public void addCluster(Cluster cluster)
    {
        this.clusters.put(cluster.getName(), cluster);
        this.clustersById.put(cluster.getId(), cluster);
    }

    public boolean containsCluster(String name)
    {
        return this.clusters.containsKey(name);
    }
    
    public boolean containsCluster(UUID id)
    {
        return this.clustersById.containsKey(id);
    }

    public Collection<Cluster> getClusters()
    {
        return this.clusters.values();
    }
    
    public Set<Location> getRootLocations()
    {
        return this.locations.values().stream().filter((e) -> {return e.getLocation() == null;}).collect(Collectors.toSet());
    }
}
