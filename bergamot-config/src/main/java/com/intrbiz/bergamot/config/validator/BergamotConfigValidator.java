package com.intrbiz.bergamot.config.validator;

import java.util.List;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.AccessControlCfg;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.bergamot.config.model.NamedObjectCfg;
import com.intrbiz.bergamot.config.model.NotifyCfg;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.config.model.SecurityDomainCfg;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.model.TrapCfg;

public class BergamotConfigValidator extends BergamotConfigResolver
{
    private final BergamotCfg cfg;
    
    public  BergamotConfigValidator(BergamotCfg cfg)
    {
        super(cfg);
        this.cfg = cfg;
    }
    
    public  BergamotConfigValidator(BergamotCfg cfg, BergamotObjectLocator... locators)
    {
        super(BergamotObjectLocator.from(cfg, locators));
        this.cfg = cfg;
    }
    
    /**
     * Validate this Bergamot configuration.  Note: you must validate 
     * a the configuration before using it.
     */
    public ValidatedBergamotConfiguration validate()
    {
        BergamotValidationReport report = new BergamotValidationReport(this.cfg.getSite());
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
        return new ValidatedBergamotConfiguration(this.cfg, report);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void computeInheritenance(BergamotValidationReport report)
    {
        // walk the object tree and compute the inheritance graph
        for (List<? extends TemplatedObjectCfg<?>> objects : this.cfg.getAllObjects())
        {
            for (TemplatedObjectCfg object : objects)
            {
                this.computeInheritenance(object, report);
            }
        }
    }
    
    private void validateGroups(BergamotValidationReport report)
    {
        for (GroupCfg group : this.cfg.getGroups())
        {
            for (String groupName : group.getGroups())
            {
                this.checkGroupExists(groupName, group, report);
            }
            for (String securityDomainName : group.getSecurityDomains())
            {
                this.checkSecurityDomainExists(securityDomainName, group, report);
            }
        }
    }
    
    private void validateHosts(BergamotValidationReport report)
    {
        for (HostCfg host : this.cfg.getHosts())
        {
            for (String groupName : host.getGroups())
            {
                this.checkGroupExists(groupName, host, report);
            }
            for (String securityDomainName : host.getSecurityDomains())
            {
                this.checkSecurityDomainExists(securityDomainName, host, report);
            }
            if (! Util.isEmpty(host.getLocation()))
            {
                this.checkLocationExists(host.getLocation(), host, report);
            }
            // check command for this host
            if (host.getCheckCommand() != null)
            {
                checkCommandExists(host.getCheckCommand().getCommand(), host, report);
            }
            // services of the host
            for (ServiceCfg service : host.getServices())
            {
                for (String groupName : service.getGroups())
                {
                    this.checkGroupExists(groupName, service, report);
                }
                for (String securityDomainName : service.getSecurityDomains())
                {
                    this.checkSecurityDomainExists(securityDomainName, service, report);
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
                for (String securityDomainName : trap.getSecurityDomains())
                {
                    this.checkSecurityDomainExists(securityDomainName, trap, report);
                }
                this.validateNotify(trap.getNotify(), trap, report);
            }
        }
    }
    
    private void validateClusters(BergamotValidationReport report)
    {
        for (ClusterCfg cluster : this.cfg.getClusters())
        {
            for (String groupName : cluster.getGroups())
            {
                this.checkGroupExists(groupName, cluster, report);
            }
            for (String securityDomainName : cluster.getSecurityDomains())
            {
                this.checkSecurityDomainExists(securityDomainName, cluster, report);
            }
            // resources of the cluster
            for (ResourceCfg resource : cluster.getResources())
            {
                for (String groupName : resource.getGroups())
                {
                    this.checkGroupExists(groupName, resource, report);
                }
                for (String securityDomainName : resource.getSecurityDomains())
                {
                    this.checkSecurityDomainExists(securityDomainName, resource, report);
                }
                this.validateNotify(resource.getNotify(), resource, report);
            }
        }
    }
    
    private void validateServices(BergamotValidationReport report)
    {
        for (ServiceCfg service : this.cfg.getServices())
        {
            for (String groupName : service.getGroups())
            {
                this.checkGroupExists(groupName, service, report);
            }
            for (String securityDomainName : service.getSecurityDomains())
            {
                this.checkSecurityDomainExists(securityDomainName, service, report);
            }
            if (service.getTemplate() == null || service.getTemplate() == false)
            {
                service.setTemplate(true);
                report.logWarn("Top level services must be templates: " + service);
            }
            this.validateNotify(service.getNotify(), service, report);
            // check command for this service
            if (service.getCheckCommand() != null)
            {
                checkCommandExists(service.getCheckCommand().getCommand(), service, report);
            }
        }
    }
    
    private void validateTraps(BergamotValidationReport report)
    {
        for (TrapCfg trap : this.cfg.getTraps())
        {
            for (String groupName : trap.getGroups())
            {
                this.checkGroupExists(groupName, trap, report);
            }
            for (String securityDomainName : trap.getSecurityDomains())
            {
                this.checkSecurityDomainExists(securityDomainName, trap, report);
            }
            if (trap.getTemplate() == null || trap.getTemplate() == false)
            {
                trap.setTemplate(true);
                report.logWarn("Top level traps must be templates: " + trap);
            }
            this.validateNotify(trap.getNotify(), trap, report);
            // check command for this trap
            if (trap.getCheckCommand() != null)
            {
                checkCommandExists(trap.getCheckCommand().getCommand(), trap, report);
            }
        }
    }
    
    private void validateResources(BergamotValidationReport report)
    {
        for (ResourceCfg resource : this.cfg.getResources())
        {
            for (String groupName : resource.getGroups())
            {
                this.checkGroupExists(groupName, resource, report);
            }
            for (String securityDomainName : resource.getSecurityDomains())
            {
                this.checkSecurityDomainExists(securityDomainName, resource, report);
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
        for (LocationCfg location : this.cfg.getLocations())
        {
            if (! Util.isEmpty(location.getLocation()))
            {
                this.checkLocationExists(location.getLocation(), location, report);
            }
            for (String securityDomainName : location.getSecurityDomains())
            {
                this.checkSecurityDomainExists(securityDomainName, location, report);
            }
        }
    }
    
    private void validateTeams(BergamotValidationReport report)
    {
        for (TeamCfg team : this.cfg.getTeams())
        {
            for (String teamName : team.getTeams())
            {
                this.checkTeamExists(teamName, team, report);
            }
            // validate access controls
            for (AccessControlCfg accessControl : team.getAccessControls())
            {
                this.checkSecurityDomainExists(accessControl.getSecurityDomain(), team, report);
            }
        }
    }
    
    private void validateContacts(BergamotValidationReport report)
    {
        for (ContactCfg contact : this.cfg.getContacts())
        {
            for (String teamName : contact.getTeams())
            {
                this.checkTeamExists(teamName, contact, report);
            }
            // validate access controls
            for (AccessControlCfg accessControl : contact.getAccessControls())
            {
                this.checkSecurityDomainExists(accessControl.getSecurityDomain(), contact, report);
            }
        }
    }
    
    private void validateCommands(BergamotValidationReport report)
    {
        for (CommandCfg command : this.cfg.getCommands())
        {
            if (Util.isEmpty(command.resolve().getEngine()))
            {
                report.logError("The command engine should be specified for " + command);
            }
        }
    }
    
    // checks
    
    private void checkSecurityDomainExists(String name, NamedObjectCfg<?> user, BergamotValidationReport report)
    {
        SecurityDomainCfg domain = this.lookup(SecurityDomainCfg.class, name);
        if (domain == null)
        {
            report.logError("Cannot find the security domain '" + name + "' referenced by " + user);
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
    
    private void checkCommandExists(String name, NamedObjectCfg<?> user, BergamotValidationReport report)
    {
        CommandCfg command = this.lookup(CommandCfg.class, name);
        if (command == null)
        {
            report.logError("Cannot find the command '" + name + "' referenced by " + user);
        }
    }

}
