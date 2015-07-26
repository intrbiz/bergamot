package com.intrbiz.bergamot.config.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.validator.BergamotConfigValidator;
import com.intrbiz.bergamot.config.validator.BergamotObjectLocator;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.configuration.Configuration;

@XmlType(name = "bergamot")
@XmlRootElement(name = "bergamot")
public class BergamotCfg extends Configuration implements BergamotObjectLocator
{
    private static final long serialVersionUID = 1L;

    public static final Class<?>[] OBJECT_TYPES = { TeamCfg.class, ContactCfg.class, TimePeriodCfg.class, LocationCfg.class, GroupCfg.class, CommandCfg.class, ServiceCfg.class, TrapCfg.class, HostCfg.class, ResourceCfg.class, ClusterCfg.class };

    private String site = "default";

    private String summary;

    private String description;

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
    
    private List<SecurityDomainCfg> securityDomains = new LinkedList<SecurityDomainCfg>();

    private Map<String, TemplatedObjectCfg<?>> index = new HashMap<String, TemplatedObjectCfg<?>>();

    public BergamotCfg()
    {
        super();
    }

    public BergamotCfg(String site, TemplatedObjectCfg<?>... objects)
    {
        super();
        this.site = site;
        for (TemplatedObjectCfg<?> object : objects)
        {
            this.addObject(object);
        }
    }
    
    public BergamotCfg(String site, String summary, String description)
    {
        super();
        this.site = site;
        this.summary = summary;
        this.description = description;
    }

    @XmlElement(name = "summary")
    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    @XmlElement(name = "description")
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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
    
    @XmlElementRef(type = SecurityDomainCfg.class)
    public List<SecurityDomainCfg> getSecurityDomains()
    {
        return securityDomains;
    }

    public void setSecurityDomains(List<SecurityDomainCfg> securityDomains)
    {
        this.securityDomains = securityDomains;
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
        this.securityDomains.addAll(other.getSecurityDomains());
        // merge in parameters
        this.getParameters().addAll(other.getParameters());
        // update the index
        this.index(true);
    }

    public void addObjects(Collection<TemplatedObjectCfg<?>> objects)
    {
        for (TemplatedObjectCfg<?> object : objects)
        {
            this.addObject(object);
        }
        // update the index
        this.index(true);
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
        else if (object instanceof SecurityDomainCfg)
        {
            this.securityDomains.add((SecurityDomainCfg) object);
        }
        // update the index
        this.index(true);
    }

    @SuppressWarnings("unchecked")
    public List<? extends TemplatedObjectCfg<?>>[] getAllObjects()
    {
        return new List[] { this.clusters, this.commands, this.contacts, this.groups, this.hosts, this.locations, this.resources, this.services, this.teams, this.timePeriods, this.traps, this.securityDomains };
    }

    public void index(boolean force)
    {
        if (force || this.index.isEmpty())
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
    }

    @SuppressWarnings("unchecked")
    public <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name)
    {
        this.index(false);
        return (T) this.index.get(type.getSimpleName() + "::" + name);
    }

    /**
     * Validate this Bergamot configuration. Note: you must validate a the configuration before using it.
     */
    public ValidatedBergamotConfiguration validate()
    {
        return new BergamotConfigValidator(this).validate();
    }
    
    public ValidatedBergamotConfiguration validate(BergamotObjectLocator... additionalLocators)
    {
        return new BergamotConfigValidator(this, additionalLocators).validate();
    }
}
