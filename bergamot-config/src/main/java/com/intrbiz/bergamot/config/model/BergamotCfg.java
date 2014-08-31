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

    private void computeInheritenance(BergamotValidationReport report)
    {
        this.index();
        // walk the object tree and compute the inheritance graph
        for (List<? extends TemplatedObjectCfg<?>> objects : this.getAllObjects())
        {
            for (TemplatedObjectCfg<?> object : objects)
            {
                this.resolveInherit(object, report);
                // process any child templated objects
                for (TemplatedObjectCfg<?> child : object.getTemplatedChildObjects())
                {
                    this.resolveInherit(child, report);
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void resolveInherit(TemplatedObjectCfg<?> object, BergamotValidationReport report)
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
                report.logError("Cannot find the inherited " + object.getClass().getSimpleName() + " named '" + inheritsFrom + "' which is inherited by " + object);
            }
        }
        if (object instanceof TimePeriodCfg)
        {
            this.resolveExcludes((TimePeriodCfg) object, report);
        }
    }
    
    private void resolveExcludes(TimePeriodCfg object, BergamotValidationReport report)
    {
        for (String exclude : object.getExcludes())
        {
            TimePeriodCfg excludedTimePeriod = this.lookup(TimePeriodCfg.class, exclude);
            if (excludedTimePeriod == null)
            {
                // error
                report.logError("Cannot find the excluded time period named '" + exclude + "' which is excluded by " + object);
            }
        }
    }
    
    /**
     * Validate this Bergamot configuration.  Note: you must validate 
     * a the configuration before using it.
     */
    public ValidatedBergamotConfiguration validate()
    {
        BergamotValidationReport report = new BergamotValidationReport(this.getSite());
        // compute the inheritance
        this.computeInheritenance(report);
        // validate the objects
        this.validateGroups(report);
        this.validateLocations(report);
        this.validateHosts(report);
        this.validateClusters(report);
        this.validateResources(report);
        this.validateServices(report);
        this.validateTraps(report);
        this.validateTeams(report);
        this.validateContacts(report);
        this.validateCommands(report);
        return new ValidatedBergamotConfiguration(this, report);
    }
    
    private void validateGroups(BergamotValidationReport report)
    {
        for (GroupCfg group : this.groups)
        {
            for (String groupName : group.getGroups())
            {
                this.checkGroupExists(groupName, group, report);
            }
        }
    }
    
    private void validateHosts(BergamotValidationReport report)
    {
        for (HostCfg host : this.hosts)
        {
            for (String groupName : host.getGroups())
            {
                this.checkGroupExists(groupName, host, report);
            }
            if (! Util.isEmpty(host.getLocation()))
            {
                this.checkLocationExists(host.getLocation(), host, report);
            }
            // services of the host
            for (ServiceCfg service : host.getServices())
            {
                for (String groupName : service.getGroups())
                {
                    this.checkGroupExists(groupName, service, report);
                }
                this.validateNotify(service.getNotify(), service, report);
            }
            // traps of the host
            for (TrapCfg trap : host.getTraps())
            {
                for (String groupName : trap.getGroups())
                {
                    this.checkGroupExists(groupName, trap, report);
                }
                this.validateNotify(trap.getNotify(), trap, report);
            }
        }
    }
    
    private void validateClusters(BergamotValidationReport report)
    {
        for (ClusterCfg cluster : this.clusters)
        {
            for (String groupName : cluster.getGroups())
            {
                this.checkGroupExists(groupName, cluster, report);
            }
            // resources of the cluster
            for (ResourceCfg resource : cluster.getResources())
            {
                for (String groupName : resource.getGroups())
                {
                    this.checkGroupExists(groupName, resource, report);
                }
                this.validateNotify(resource.getNotify(), resource, report);
            }
        }
    }
    
    private void validateServices(BergamotValidationReport report)
    {
        for (ServiceCfg service : this.services)
        {
            for (String groupName : service.getGroups())
            {
                this.checkGroupExists(groupName, service, report);
            }
            if (service.getTemplate() == null || service.getTemplate() == false)
            {
                service.setTemplate(true);
                report.logWarn("Top level services must be templates: " + service);
            }
            this.validateNotify(service.getNotify(), service, report);
        }
    }
    
    private void validateTraps(BergamotValidationReport report)
    {
        for (TrapCfg trap : this.traps)
        {
            for (String groupName : trap.getGroups())
            {
                this.checkGroupExists(groupName, trap, report);
            }
            if (trap.getTemplate() == null || trap.getTemplate() == false)
            {
                trap.setTemplate(true);
                report.logWarn("Top level traps must be templates: " + trap);
            }
            this.validateNotify(trap.getNotify(), trap, report);
        }
    }
    
    private void validateResources(BergamotValidationReport report)
    {
        for (ResourceCfg resource : this.resources)
        {
            for (String groupName : resource.getGroups())
            {
                this.checkGroupExists(groupName, resource, report);
            }
            if (resource.getTemplate() == null || resource.getTemplate() == false)
            {
                resource.setTemplate(true);
                report.logWarn("Top level resources must be templates: " + resource);
            }
            this.validateNotify(resource.getNotify(), resource, report);
        }
    }
    
    private void validateNotify(NotifyCfg notify, NamedObjectCfg<?> of, BergamotValidationReport report)
    {
        if (notify != null)
        {
            for (String team : notify.getTeams())
            {
                this.checkTeamExists(team, of, report);
            }
            for (String contact : notify.getContacts())
            {
                this.checkContactExists(contact, of, report);
            }
        }
    }
    
    private void validateLocations(BergamotValidationReport report)
    {
        for (LocationCfg location : this.locations)
        {
            if (! Util.isEmpty(location.getLocation()))
            {
                this.checkLocationExists(location.getLocation(), location, report);
            }
        }
    }
    
    private void validateTeams(BergamotValidationReport report)
    {
        for (TeamCfg team : this.teams)
        {
            for (String teamName : team.getTeams())
            {
                this.checkTeamExists(teamName, team, report);
            }
        }
    }
    
    private void validateContacts(BergamotValidationReport report)
    {
        for (ContactCfg contact : this.contacts)
        {
            for (String teamName : contact.getTeams())
            {
                this.checkTeamExists(teamName, contact, report);
            }
        }
    }
    
    private void validateCommands(BergamotValidationReport report)
    {
        for (CommandCfg command : this.commands)
        {
            if (Util.isEmpty(command.resolve().getEngine()))
            {
                report.logError("The command engine should be specified for " + command);
            }
        }
    }
    
    private void checkGroupExists(String name, NamedObjectCfg<?> user, BergamotValidationReport report)
    {
        GroupCfg group = this.lookup(GroupCfg.class, name);
        if (group == null)
        {
            report.logError("Cannot find the group '" + name + "' referenced by " + user);
        }
    }
    
    private void checkLocationExists(String name, NamedObjectCfg<?> user, BergamotValidationReport report)
    {
        LocationCfg location = this.lookup(LocationCfg.class, name);
        if (location == null)
        {
            report.logError("Cannot find the location '" + name + "' referenced by " + user);
        }
    }
    
    private void checkTeamExists(String name, NamedObjectCfg<?> user, BergamotValidationReport report)
    {
        TeamCfg team = this.lookup(TeamCfg.class, name);
        if (team == null)
        {
            report.logError("Cannot find the team '" + name + "' referenced by " + user);
        }
    }
    
    private void checkContactExists(String name, NamedObjectCfg<?> user, BergamotValidationReport report)
    {
        ContactCfg contact = this.lookup(ContactCfg.class, name);
        if (contact == null)
        {
            report.logError("Cannot find the contact '" + name + "' referenced by " + user);
        }
    }
    
    public static class BergamotValidationReport
    {
        private final String site;
        
        private boolean valid = true;
        
        private List<String> errors = new LinkedList<String>();
        
        private List<String> warnings = new LinkedList<String>();
                
        public BergamotValidationReport(String site)
        {
            this.site = site;
        }
        
        public String getSite()
        {
            return this.site;
        }
        
        public boolean isValid()
        {
            return this.valid;
        }
        
        public List<String> getErrors()
        {
            return this.errors;
        }
        
        public List<String> getWarnings()
        {
            return this.warnings;
        }
        
        private void logError(String message)
        {
            this.valid = false;
            this.errors.add(message);
        }
        
        private void logWarn(String message)
        {
            this.errors.add(message);
        }
        
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("The configuration for site '").append(this.getSite()).append("' is ").append(this.isValid() ? "valid." : "invalid!").append("\n");
            if (this.warnings.size() > 0 || this.errors.size() > 0) sb.append("  Warnings: ").append(this.warnings.size()).append(", Errors: ").append(this.errors.size()).append("\n");
            for (String error : this.errors)
            {
                sb.append("Error: ").append(error).append("\n");
            }
            for (String warn : this.warnings)
            {
                sb.append("Warn: ").append(warn).append("\n");
            }
            return sb.toString();
        }
    }
    
    public static class ValidatedBergamotConfiguration
    {
        private final BergamotCfg config;
        
        private final BergamotValidationReport report;
        
        private ValidatedBergamotConfiguration(BergamotCfg config, BergamotValidationReport report)
        {
            this.config = config;
            this.report = report;
        }

        public BergamotCfg getConfig()
        {
            return config;
        }

        public BergamotValidationReport getReport()
        {
            return report;
        }
    }
}
