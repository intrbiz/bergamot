package com.intrbiz.bergamot.config.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.Util;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "bergamot")
@XmlRootElement(name = "bergamot")
public class BergamotCfg extends Configuration
{
    private static final long serialVersionUID = 1L;
    
    private String site = "default";

    private List<TeamCfg> teams = new LinkedList<TeamCfg>();

    private List<ContactCfg> contacts = new LinkedList<ContactCfg>();

    private List<TimePeriodCfg> timePeriods = new LinkedList<TimePeriodCfg>();

    private List<LocationCfg> locations = new LinkedList<LocationCfg>();

    private List<GroupCfg> groups = new LinkedList<GroupCfg>();

    private List<CommandCfg> commands = new LinkedList<CommandCfg>();

    private List<ServiceCfg> services = new LinkedList<ServiceCfg>();

    private List<TrapCfg> traps = new LinkedList<TrapCfg>();

    private List<HostCfg> hosts = new LinkedList<HostCfg>();

    private List<ResourceCfg> resources = new LinkedList<ResourceCfg>();

    private List<ClusterCfg> clusters = new LinkedList<ClusterCfg>();

    private Map<String, TemplatedObjectCfg<?>> index = new HashMap<String, TemplatedObjectCfg<?>>();

    public BergamotCfg()
    {
        super();
    }

    @XmlElementRef(type = TeamCfg.class)
    public List<TeamCfg> getTeams()
    {
        return teams;
    }

    public void setTeams(List<TeamCfg> teams)
    {
        this.teams = teams;
    }

    @XmlAttribute(name = "site")
    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
    {
        this.site = site;
    }

    @XmlElementRef(type = ContactCfg.class)
    public List<ContactCfg> getContacts()
    {
        return contacts;
    }

    public void setContacts(List<ContactCfg> contacts)
    {
        this.contacts = contacts;
    }

    @XmlElementRef(type = TimePeriodCfg.class)
    public List<TimePeriodCfg> getTimePeriods()
    {
        return timePeriods;
    }

    public void setTimePeriods(List<TimePeriodCfg> timePeriods)
    {
        this.timePeriods = timePeriods;
    }

    @XmlElementRef(type = LocationCfg.class)
    public List<LocationCfg> getLocations()
    {
        return locations;
    }

    public void setLocations(List<LocationCfg> locations)
    {
        this.locations = locations;
    }

    @XmlElementRef(type = GroupCfg.class)
    public List<GroupCfg> getGroups()
    {
        return groups;
    }

    public void setGroups(List<GroupCfg> groups)
    {
        this.groups = groups;
    }

    @XmlElementRef(type = CommandCfg.class)
    public List<CommandCfg> getCommands()
    {
        return commands;
    }

    public void setCommands(List<CommandCfg> commands)
    {
        this.commands = commands;
    }

    @XmlElementRef(type = ServiceCfg.class)
    public List<ServiceCfg> getServices()
    {
        return services;
    }

    public void setServices(List<ServiceCfg> services)
    {
        this.services = services;
    }

    @XmlElementRef(type = HostCfg.class)
    public List<HostCfg> getHosts()
    {
        return hosts;
    }

    public void setHosts(List<HostCfg> hosts)
    {
        this.hosts = hosts;
    }

    @XmlElementRef(type = TrapCfg.class)
    public List<TrapCfg> getTraps()
    {
        return traps;
    }

    public void setTraps(List<TrapCfg> traps)
    {
        this.traps = traps;
    }

    @XmlElementRef(type = ResourceCfg.class)
    public List<ResourceCfg> getResources()
    {
        return resources;
    }

    public void setResources(List<ResourceCfg> resources)
    {
        this.resources = resources;
    }

    @XmlElementRef(type = ClusterCfg.class)
    public List<ClusterCfg> getClusters()
    {
        return clusters;
    }

    public void setClusters(List<ClusterCfg> clusters)
    {
        this.clusters = clusters;
    }

    public void mergeIn(BergamotCfg other)
    {
        this.teams.addAll(other.getTeams());
        this.contacts.addAll(other.getContacts());
        this.timePeriods.addAll(other.getTimePeriods());
        this.locations.addAll(other.getLocations());
        this.groups.addAll(other.getGroups());
        this.commands.addAll(other.getCommands());
        this.services.addAll(other.getServices());
        this.hosts.addAll(other.getHosts());
        this.traps.addAll(other.getTraps());
        this.resources.addAll(other.getResources());
        this.clusters.addAll(other.getClusters());
    }
    
    public void addObject(TemplatedObjectCfg<?> object)
    {
        if (object instanceof ClusterCfg)
        {
            this.clusters.add((ClusterCfg) object);
        }
        else if (object instanceof CommandCfg)
        {
            this.commands.add((CommandCfg) object);
        }
        else if (object instanceof ContactCfg)
        {
            this.contacts.add((ContactCfg) object);
        }
        else if (object instanceof GroupCfg)
        {
            this.groups.add((GroupCfg) object);
        }
        else if (object instanceof HostCfg)
        {
            this.hosts.add((HostCfg) object);
        }
        else if (object instanceof LocationCfg)
        {
            this.locations.add((LocationCfg) object);
        }
        else if (object instanceof ResourceCfg)
        {
            this.resources.add((ResourceCfg) object);
        }
        else if (object instanceof ServiceCfg)
        {
            this.services.add((ServiceCfg) object);
        }
        else if (object instanceof TeamCfg)
        {
            this.teams.add((TeamCfg) object);
        }
        else if (object instanceof TimePeriodCfg)
        {
            this.timePeriods.add((TimePeriodCfg) object);
        }
        else if (object instanceof TrapCfg)
        {
            this.traps.add((TrapCfg) object);
        }
    }

    @SuppressWarnings("unchecked")
    public List<? extends TemplatedObjectCfg<?>>[] getAllObjects()
    {
        return new List[] { this.clusters, this.commands, this.contacts, this.groups, this.hosts, this.locations, this.resources, this.services, this.teams, this.timePeriods, this.traps };
    }

    public void index()
    {
        this.index.clear();
        for (List<? extends TemplatedObjectCfg<?>> objects : this.getAllObjects())
        {
            for (TemplatedObjectCfg<?> object : objects)
            {
                if (!Util.isEmpty(object.getName()))
                {
                    this.index.put(object.getClass().getSimpleName() + "::" + object.getName(), object);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name)
    {
        return (T) this.index.get(type.getSimpleName() + "::" + name);
    }

    public void computeInheritenance()
    {
        this.index();
        // walk the object tree and compute the inheritance graph
        for (List<? extends TemplatedObjectCfg<?>> objects : this.getAllObjects())
        {
            for (TemplatedObjectCfg<?> object : objects)
            {
                this.resolveInherit(object);
                // process any child templated objects
                for (TemplatedObjectCfg<?> child : object.getTemplatedChildObjects())
                {
                    this.resolveInherit(child);
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void resolveInherit(TemplatedObjectCfg<?> object)
    {
        for (String inheritsFrom : object.getInheritedTemplates())
        {
            TemplatedObjectCfg<?> superObject = this.lookup(object.getClass(), inheritsFrom);
            if (superObject != null)
            {
                ((TemplatedObjectCfg) object).addInheritedObject(superObject);
            }
            else
            {
                // error
                System.err.println("Error: Cannot find the inherited " + object.getClass().getSimpleName() + " named '" + inheritsFrom + "' which is inherited by " + object);
            }
        }
        if (object instanceof TimePeriodCfg)
        {
            this.resolveExcludes((TimePeriodCfg) object);
        }
    }
    
    private void resolveExcludes(TimePeriodCfg object)
    {
        for (String exclude : object.getExcludes())
        {
            TimePeriodCfg excludedTimePeriod = this.lookup(TimePeriodCfg.class, exclude);
            if (excludedTimePeriod == null)
            {
                // error
                System.err.println("Error: Cannot find the excluded time period named '" + exclude + "' which is excluded by " + object);
            }
        }
    }
    
    public void validate()
    {
        this.validateGroups();
        this.validateLocations();
        this.validateHosts();
        this.validateClusters();
        this.validateResources();
        this.validateServices();
        this.validateTraps();
        this.validateTeams();
        this.validateContacts();
        this.validateCommands();
    }
    
    private void validateGroups()
    {
        for (GroupCfg group : this.groups)
        {
            for (String groupName : group.getGroups())
            {
                this.checkGroupExists(groupName, group);
            }
        }
    }
    
    private void validateHosts()
    {
        for (HostCfg host : this.hosts)
        {
            for (String groupName : host.getGroups())
            {
                this.checkGroupExists(groupName, host);
            }
            if (! Util.isEmpty(host.getLocation()))
            {
                this.checkLocationExists(host.getLocation(), host);
            }
            // services of the host
            for (ServiceCfg service : host.getServices())
            {
                for (String groupName : service.getGroups())
                {
                    this.checkGroupExists(groupName, service);
                }
                this.validateNotify(service.getNotify(), service);
            }
            // traps of the host
            for (TrapCfg trap : host.getTraps())
            {
                for (String groupName : trap.getGroups())
                {
                    this.checkGroupExists(groupName, trap);
                }
                this.validateNotify(trap.getNotify(), trap);
            }
        }
    }
    
    private void validateClusters()
    {
        for (ClusterCfg cluster : this.clusters)
        {
            for (String groupName : cluster.getGroups())
            {
                this.checkGroupExists(groupName, cluster);
            }
            // resources of the cluster
            for (ResourceCfg resource : cluster.getResources())
            {
                for (String groupName : resource.getGroups())
                {
                    this.checkGroupExists(groupName, resource);
                }
                this.validateNotify(resource.getNotify(), resource);
            }
        }
    }
    
    private void validateServices()
    {
        for (ServiceCfg service : this.services)
        {
            for (String groupName : service.getGroups())
            {
                this.checkGroupExists(groupName, service);
            }
            if (service.getTemplate() == null || service.getTemplate() == false)
            {
                service.setTemplate(true);
                System.err.println("Warn: Top level services must be templates: " + service);
            }
            this.validateNotify(service.getNotify(), service);
        }
    }
    
    private void validateTraps()
    {
        for (TrapCfg trap : this.traps)
        {
            for (String groupName : trap.getGroups())
            {
                this.checkGroupExists(groupName, trap);
            }
            if (trap.getTemplate() == null || trap.getTemplate() == false)
            {
                trap.setTemplate(true);
                System.err.println("Warn: Top level traps must be templates: " + trap);
            }
            this.validateNotify(trap.getNotify(), trap);
        }
    }
    
    private void validateResources()
    {
        for (ResourceCfg resource : this.resources)
        {
            for (String groupName : resource.getGroups())
            {
                this.checkGroupExists(groupName, resource);
            }
            if (resource.getTemplate() == null || resource.getTemplate() == false)
            {
                resource.setTemplate(true);
                System.err.println("Warn: Top level resources must be templates: " + resource);
            }
            this.validateNotify(resource.getNotify(), resource);
        }
    }
    
    private void validateNotify(NotifyCfg notify, NamedObjectCfg<?> of)
    {
        if (notify != null)
        {
            for (String team : notify.getTeams())
            {
                this.checkTeamExists(team, of);
            }
            for (String contact : notify.getContacts())
            {
                this.checkContactExists(contact, of);
            }
        }
    }
    
    private void validateLocations()
    {
        for (LocationCfg location : this.locations)
        {
            if (! Util.isEmpty(location.getLocation()))
            {
                this.checkLocationExists(location.getLocation(), location);
            }
        }
    }
    
    private void validateTeams()
    {
        for (TeamCfg team : this.teams)
        {
            for (String teamName : team.getTeams())
            {
                this.checkTeamExists(teamName, team);
            }
        }
    }
    
    private void validateContacts()
    {
        for (ContactCfg contact : this.contacts)
        {
            for (String teamName : contact.getTeams())
            {
                this.checkTeamExists(teamName, contact);
            }
        }
    }
    
    private void validateCommands()
    {
        for (CommandCfg command : this.commands)
        {
            if (Util.isEmpty(command.getEngine()))
            {
                System.err.println("Warn: The command engine should be specified for " + command);
            }
        }
    }
    
    private void checkGroupExists(String name, NamedObjectCfg<?> user)
    {
        GroupCfg group = this.lookup(GroupCfg.class, name);
        if (group == null)
        {
            System.err.println("Error: Cannot find the group '" + name + "' referenced by " + user);
        }
    }
    
    private void checkLocationExists(String name, NamedObjectCfg<?> user)
    {
        LocationCfg location = this.lookup(LocationCfg.class, name);
        if (location == null)
        {
            System.err.println("Error: Cannot find the location '" + name + "' referenced by " + user);
        }
    }
    
    private void checkTeamExists(String name, NamedObjectCfg<?> user)
    {
        TeamCfg team = this.lookup(TeamCfg.class, name);
        if (team == null)
        {
            System.err.println("Error: Cannot find the team '" + name + "' referenced by " + user);
        }
    }
    
    private void checkContactExists(String name, NamedObjectCfg<?> user)
    {
        ContactCfg contact = this.lookup(ContactCfg.class, name);
        if (contact == null)
        {
            System.err.println("Error: Cannot find the contact '" + name + "' referenced by " + user);
        }
    }
}
