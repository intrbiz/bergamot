package com.intrbiz.bergamot.config;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.command.NagiosCommandString;
import com.intrbiz.bergamot.compat.config.builder.NagiosConfigBuilder;
import com.intrbiz.bergamot.compat.config.model.NagiosCommandCfg;
import com.intrbiz.bergamot.compat.config.model.NagiosContactCfg;
import com.intrbiz.bergamot.compat.config.model.NagiosContactgroupCfg;
import com.intrbiz.bergamot.compat.config.model.NagiosHostCfg;
import com.intrbiz.bergamot.compat.config.model.NagiosHostgroupCfg;
import com.intrbiz.bergamot.compat.config.model.NagiosServiceCfg;
import com.intrbiz.bergamot.compat.config.model.NagiosServicegroupCfg;
import com.intrbiz.bergamot.compat.config.model.NagiosTimeperiodCfg;
import com.intrbiz.bergamot.compat.macro.MacroFrame;
import com.intrbiz.bergamot.compat.macro.MacroProcessor;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.CheckCommandCfg;
import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.model.NotificationsCfg;
import com.intrbiz.bergamot.config.model.NotifyCfg;
import com.intrbiz.bergamot.config.model.ScheduleCfg;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.config.model.StateCfg;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.timerange.TimeRangeParser;
import com.intrbiz.configuration.CfgParameter;

public class NagiosConfigConverter
{
    private BergamotCfg config;

    private NagiosConfigBuilder nagiosConfig;
    
    private File baseDir;

    public NagiosConfigConverter()
    {
    }

    public NagiosConfigConverter site(String site)
    {
        this.config = new BergamotCfg();
        this.config.setSite(site);
        return this;
    }
    
    public NagiosConfigConverter baseDir(File baseDir)
    {
        this.baseDir = baseDir;
        return this;
    }

    public NagiosConfigConverter nagiosConfig(NagiosConfigBuilder nagiosConfig)
    {
        this.nagiosConfig = nagiosConfig;
        return this;
    }

    public BergamotCfg convert()
    {
        // teams and contacts
        this.convertContactGroups();
        this.convertContacts();
        // commands
        this.convertCommands();
        // time periods
        this.convertTimePeriods();
        // groups
        this.convertGroups();
        // services
        this.convertServices();
        // hosts
        this.convertHosts();
        // link
        this.config.index();
        this.linkContacts();
        this.linkServices();
        this.linkHostgroups();
        this.linkServicegroups();
        //
        return this.config;
    }

    private void linkHostgroups()
    {
        for (NagiosHostgroupCfg cfg : this.nagiosConfig.getHostgroups())
        {
            GroupCfg group = this.config.lookup(GroupCfg.class, cfg.getHostgroupName());
            if (group != null)
            {
                // child hostgroups
                if (cfg.getHostgroupMembers() != null)
                {
                    for (String child : cfg.getHostgroupMembers())
                    {
                        GroupCfg childGroup = this.config.lookup(GroupCfg.class, child);
                        if (childGroup != null)
                        {
                            childGroup.addGroup(group.getName());
                            childGroup.removeGroup("hosts");
                        }
                    }
                }
                // hosts
                if (cfg.getMembers() != null)
                {
                    for (String hostName : cfg.getMembers())
                    {
                        HostCfg host = this.config.lookup(HostCfg.class, hostName);
                        if (host != null)
                        {
                            host.addGroup(group.getName());
                        }
                    }
                }
            }
        }
    }

    private void linkServicegroups()
    {
        for (NagiosServicegroupCfg cfg : this.nagiosConfig.getServicegroups())
        {
            GroupCfg group = this.config.lookup(GroupCfg.class, cfg.getServicegroupName());
            if (group != null)
            {
                // child service
                if (cfg.getServicegroupMembers() != null)
                {
                    for (String child : cfg.getServicegroupMembers())
                    {
                        GroupCfg childGroup = this.config.lookup(GroupCfg.class, child);
                        if (childGroup != null)
                        {
                            childGroup.addGroup(group.getName());
                            childGroup.removeGroup("hosts");
                        }
                    }
                }
                // services
                if (cfg.getMembers() != null)
                {
                    for (Iterator<String> i = cfg.getMembers().iterator(); i.hasNext();)
                    {
                        String hostName = i.next();
                        String serviceDescription = i.next();
                        //
                        HostCfg host = this.config.lookup(HostCfg.class, hostName);
                        if (host != null)
                        {
                            ServiceCfg service = host.lookupService(serviceDescription);
                            if (service != null)
                            {
                                service.addGroup(group.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    private void linkContacts()
    {
        for (NagiosContactgroupCfg contactgroup : this.nagiosConfig.getContactgroups())
        {
            TeamCfg team = this.config.lookup(TeamCfg.class, contactgroup.getContactgroupName());
            if (team != null)
            {
                if (contactgroup.getMembers() != null)
                {
                    for (String contactName : contactgroup.getMembers())
                    {
                        ContactCfg contact = this.config.lookup(ContactCfg.class, contactName);
                        if (contact != null)
                        {
                            contact.getTeams().add(team.getName());
                        }
                    }
                }
                if (contactgroup.getContactgroupMembers() != null)
                {
                    for (String childName : contactgroup.getContactgroupMembers())
                    {
                        TeamCfg childTeam = this.config.lookup(TeamCfg.class, childName);
                        if (childTeam != null)
                        {
                            childTeam.addTeam(team.getName());
                        }
                    }
                }
            }
        }
    }

    private void convertContacts()
    {
        for (NagiosContactCfg cfg : this.nagiosConfig.getContacts())
        {
            // convert the contact
            ContactCfg contact = new ContactCfg();
            contact.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
            contact.setTemplate(cfg.isRegister() == null || cfg.isRegister() == false ? true : null);
            contact.setName(contact.getTemplateBooleanValue() ? cfg.getName() : cfg.getContactName());
            if (cfg.getInherits() != null) contact.getInheritedTemplates().addAll(cfg.getInherits());
            if (!Util.isEmpty(cfg.getEmail())) contact.setEmail(cfg.getEmail());
            if (!Util.isEmpty(cfg.getPager())) contact.setPager(cfg.getPager());
            if (!Util.isEmpty(cfg.getAlias())) contact.setSummary(cfg.getAlias());
            // notifications
            if (cfg.isNotificationsEnabled() != null || cfg.getNotificationPeriod() != null)
            {
                NotificationsCfg notifications = new NotificationsCfg();
                notifications.setEnabled(cfg.isNotificationsEnabled());
                if (cfg.getNotificationPeriod() != null)
                {
                    notifications.setNotificationPeriod(Util.coalesce(cfg.getNotificationPeriod(), cfg.getServiceNotificationPeriod(), cfg.getHostNotificationPeriod(), "24x7"));
                }
                contact.setNotifications(notifications);
            }
            // teams
            if (cfg.getContactgroups() != null) contact.getTeams().addAll(cfg.getContactgroups());
            // add
            this.config.getContacts().add(contact);
        }
    }

    private void convertContactGroups()
    {
        for (NagiosContactgroupCfg cfg : this.nagiosConfig.getContactgroups())
        {
            // convert the contact
            TeamCfg team = new TeamCfg();
            team.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
            team.setTemplate(cfg.isRegister() == null || cfg.isRegister() == false ? true : null);
            team.setName(team.getTemplateBooleanValue() ? cfg.getName() : cfg.getContactgroupName());
            if (cfg.getInherits() != null) team.getInheritedTemplates().addAll(cfg.getInherits());
            if (!Util.isEmpty(cfg.getAlias())) team.setSummary(cfg.getAlias());
            // add
            this.config.getTeams().add(team);
        }
    }

    private void convertCommands()
    {
        for (NagiosCommandCfg cfg : this.nagiosConfig.getCommands())
        {
            // convert the command
            CommandCfg command = new CommandCfg();
            command.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
            command.setTemplate(cfg.isRegister() == null || cfg.isRegister() == false ? true : null);
            command.setName(command.getTemplateBooleanValue() ? cfg.getName() : cfg.getCommandName());
            command.setEngine("nagios");
            // command_line parameter
            String commandLine = cfg.getCommandLine();
            if (commandLine != null)
            {
                // convert the macros
                MacroFrame frame = new MacroFrame();
                frame.clearPrototypes();
                // resources
                frame.put("USER1", "#{nagios.path}");
                for (int i = 2; i < 33; i++)
                {
                    frame.put("USER" + i, "#{nagios.user" + i + "}");
                }
                // default stuff
                frame.put("HOSTADDRESS", "#{host.address}");
                frame.put("HOSTNAME", "#{host.name}");
                frame.put("SERVICEDESCRIPTION", "#{service.name}");
                // args
                for (int i = 1; i < 17; i++)
                {
                    frame.put("ARG" + i, "#{arg" + i + "}");
                }
                commandLine = MacroProcessor.applyMacros(commandLine, frame);
                // add the param
                command.addParameter(new CfgParameter("command_line", null, null, commandLine));
            }
            // add
            this.config.getCommands().add(command);
        }
    }

    private void convertTimePeriods()
    {
        for (NagiosTimeperiodCfg cfg : this.nagiosConfig.getTimeperiods())
        {
            // convert
            TimePeriodCfg timePeriod = new TimePeriodCfg();
            timePeriod.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
            timePeriod.setTemplate((cfg.isRegister() == null || cfg.isRegister() == false) ? true : false);
            timePeriod.setName(timePeriod.getTemplateBooleanValue() ? cfg.getName() : cfg.getTimeperiodName());
            if (cfg.getInherits() != null) timePeriod.getInheritedTemplates().addAll(cfg.getInherits());
            if (cfg.getExclude() != null) timePeriod.getExcludes().addAll(cfg.getExclude());
            if (!Util.isEmpty(cfg.getAlias())) timePeriod.setSummary(cfg.getAlias());
            // time ranges
            for (String period : cfg.getPeriods())
            {
                timePeriod.getTimeRanges().add(TimeRangeParser.parseTimeRange(period));
            }
            // add
            this.config.getTimePeriods().add(timePeriod);
        }
    }

    private void convertGroups()
    {
        // defacto groups
        GroupCfg hosts    = new GroupCfg("hosts", "Hosts");
        GroupCfg services = new GroupCfg("services", "Services");
        hosts.setLoadedFrom(new File(this.baseDir, "bergamot_groups.xml"));
        services.setLoadedFrom(new File(this.baseDir, "bergamot_groups.xml"));
        this.config.getGroups().add(hosts);
        this.config.getGroups().add(services);
        //
        for (NagiosHostgroupCfg cfg : this.nagiosConfig.getHostgroups())
        {
            // convert
            GroupCfg group = new GroupCfg();
            group.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
            group.setName(Util.coalesce(cfg.getHostgroupName(), cfg.getName()));
            if (!Util.isEmpty(cfg.getAlias())) group.setSummary(cfg.getAlias());
            group.addGroup(hosts.getName());
            // add
            this.config.getGroups().add(group);
        }
        for (NagiosServicegroupCfg cfg : this.nagiosConfig.getServicegroups())
        {
            // convert
            GroupCfg group = new GroupCfg();
            group.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
            group.setName(Util.coalesce(cfg.getServicegroupName(), cfg.getName()));
            if (!Util.isEmpty(cfg.getAlias())) group.setSummary(cfg.getAlias());
            group.addGroup(services.getName());
            // add
            this.config.getGroups().add(group);
        }
    }

    private void convertServices()
    {
        /*
         * TODO: Add trap handling
         * TODO: We can be smarter about how we map services, 
         *       for example, where a service is applied to a host group, 
         *       we can create a host template for that group
         */
        for (NagiosServiceCfg cfg : this.nagiosConfig.getServices())
        {
            // convert
            ServiceCfg service = new ServiceCfg();
            service.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
            service.setTemplate(true);
            service.setName(computeServiceName(cfg));
            service.setSummary(cfg.getServiceDescription());
            if (cfg.getInherits() != null) service.getInheritedTemplates().addAll(cfg.getInherits());
            // notifications
            if (cfg.isNotificationsEnabled() != null || cfg.getNotificationPeriod() != null)
            {
                service.setNotifications(new NotificationsCfg());
                service.getNotifications().setEnabled(cfg.isNotificationsEnabled());
                if (cfg.getNotificationPeriod() != null)
                {
                    service.getNotifications().setNotificationPeriod(Util.coalesce(cfg.getNotificationPeriod(), "24x7"));
                }
            }
            // notify
            if ((cfg.getContactGroups() != null && (!cfg.getContactGroups().isEmpty())) || (cfg.getContacts() != null && (!cfg.getContacts().isEmpty())))
            {
                service.setNotify(new NotifyCfg());
                if (cfg.getContactGroups() != null && (!cfg.getContactGroups().isEmpty()))
                {
                    service.getNotify().getTeams().addAll(cfg.getContactGroups());
                }
                if (cfg.getContacts() != null && (!cfg.getContacts().isEmpty()))
                {
                    service.getNotify().getContacts().addAll(cfg.getContacts());
                }
            }
            // schedule
            if (cfg.getCheckInterval() != null || cfg.getRetryInterval() != null)
            {
                service.setSchedule(new ScheduleCfg());
                service.getSchedule().setEvery(cfg.getCheckInterval());
                service.getSchedule().setRetryEvery(cfg.getRetryInterval());
                if (cfg.getCheckPeriod() != null)
                {
                    service.getSchedule().setTimePeriod(cfg.getCheckPeriod());
                }
            }
            // state
            if (cfg.getMaxCheckAttempts() != null)
            {
                service.setState(new StateCfg());
                service.getState().setFailedAfter(cfg.getMaxCheckAttempts());
                service.getState().setRecoversAfter(cfg.getMaxCheckAttempts());
            }
            // groups
            if (cfg.getServicegroups() != null)
            {
                service.getGroups().addAll(cfg.getServicegroups());
            }
            // for debugging parameters for hostgroups and hosts
            if (cfg.getHostgroupName() != null && (!cfg.getHostgroupName().isEmpty()))
            {
                service.addParameter(new CfgParameter("hostgroups", null, null, Util.join(", ", cfg.getHostgroupName())));
            }
            if (cfg.getHostName() != null && (!cfg.getHostName().isEmpty()))
            {
                service.addParameter(new CfgParameter("hosts", null, null, Util.join(", ", cfg.getHostName())));
            }
            // check command
            if (cfg.getCheckCommand() != null)
            {
                // parse the check command
                NagiosCommandString command = NagiosCommandString.parse(cfg.getCheckCommand());
                service.setCheckCommand(new CheckCommandCfg());
                service.getCheckCommand().setCommand(command.getCommandName());
                // parameters
                int i = 1;
                for (String arg : command.getArguments())
                {
                    System.out.println("Adding arg: " + arg);
                    service.getCheckCommand().addParameter(new CfgParameter("arg" + i++, null, null, arg));
                }
            }
            // add
            this.config.getServices().add(service);
        }
    }

    private void convertHosts()
    {
        for (NagiosHostCfg cfg : this.nagiosConfig.getHosts())
        {
            // convert
            HostCfg host = new HostCfg();
            host.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
            host.setTemplate(cfg.isRegister() == null || cfg.isRegister() == false ? true : null);
            host.setName(host.getTemplateBooleanValue() ? cfg.getName() : cfg.getHostName());
            host.setSummary(Util.coalesceEmpty(cfg.getAlias(), cfg.getDisplayName()));
            host.setAddress(cfg.getAddress());
            if (cfg.getInherits() != null) host.getInheritedTemplates().addAll(cfg.getInherits());
            // notifications
            if (cfg.isNotificationsEnabled() != null || cfg.getNotificationPeriod() != null)
            {
                host.setNotifications(new NotificationsCfg());
                host.getNotifications().setEnabled(cfg.isNotificationsEnabled());
                if (cfg.getNotificationPeriod() != null)
                {
                    host.getNotifications().setNotificationPeriod(Util.coalesce(cfg.getNotificationPeriod(), "24x7"));
                }
            }
            // notify
            if ((cfg.getContactGroups() != null && (!cfg.getContactGroups().isEmpty())) || (cfg.getContacts() != null && (!cfg.getContacts().isEmpty())))
            {
                host.setNotify(new NotifyCfg());
                if (cfg.getContactGroups() != null && (!cfg.getContactGroups().isEmpty()))
                {
                    host.getNotify().getTeams().addAll(cfg.getContactGroups());
                }
                if (cfg.getContacts() != null && (!cfg.getContacts().isEmpty()))
                {
                    host.getNotify().getContacts().addAll(cfg.getContacts());
                }
            }
            // schedule
            if (cfg.getCheckInterval() != null || cfg.getRetryInterval() != null)
            {
                host.setSchedule(new ScheduleCfg());
                host.getSchedule().setEvery(cfg.getCheckInterval());
                host.getSchedule().setRetryEvery(cfg.getRetryInterval());
                if (cfg.getCheckPeriod() != null)
                {
                    host.getSchedule().setTimePeriod(cfg.getCheckPeriod());
                }
            }
            // state
            if (cfg.getMaxCheckAttempts() != null)
            {
                host.setState(new StateCfg());
                host.getState().setFailedAfter(cfg.getMaxCheckAttempts());
                host.getState().setRecoversAfter(cfg.getMaxCheckAttempts());
            }
            // groups
            if (cfg.getHostgroups() != null)
            {
                host.getGroups().addAll(cfg.getHostgroups());
            }
            // check command
            if (cfg.getCheckCommand() != null)
            {
                // parse the check command
                NagiosCommandString command = NagiosCommandString.parse(cfg.getCheckCommand());
                host.setCheckCommand(new CheckCommandCfg());
                host.getCheckCommand().setCommand(command.getCommandName());
                // parameters
                int i = 1;
                for (String arg : command.getArguments())
                {
                    host.getCheckCommand().addParameter(new CfgParameter("arg" + i++, null, null, arg));
                }
            }
            // add
            this.config.getHosts().add(host);
        }
    }

    private void linkServices()
    {
        for (NagiosServiceCfg cfg : this.nagiosConfig.getServices())
        {
            List<HostCfg> hosts = new LinkedList<HostCfg>();
            // build the hosts to add the
            if (cfg.getHostName() != null)
            {
                for (String hostName : cfg.getHostName())
                {
                    HostCfg host = this.config.lookup(HostCfg.class, hostName);
                    if (host != null)
                    {
                        hosts.add(host);
                    }
                }
            }
            if (cfg.getHostgroupName() != null)
            {
                for (String hostgroupName : cfg.getHostgroupName())
                {
                    for (HostCfg host : this.config.getHosts())
                    {
                        if (host.containsGroup(hostgroupName)) hosts.add(host);
                    }
                }
            }
            // add the services
            for (HostCfg host : hosts)
            {
                ServiceCfg service = new ServiceCfg();
                service.getInheritedTemplates().add(computeServiceName(cfg));
                host.getServices().add(service);
            }
        }
    }
    
    private static File convertFile(File orig)
    {
        String name = orig.getName();
        name = name.substring(0, name.length() - 4);
        name += ".xml";
        return new File(orig.getParentFile(), name);
    }

    private static String computeServiceName(NagiosServiceCfg cfg)
    {
        if (cfg.isRegister() == null || cfg.isRegister() == false)
        {
            return cfg.getName();
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            if (cfg.getHostgroupName() != null && (!cfg.getHostgroupName().isEmpty()))
            {
                sb.append(Util.join("_", cfg.getHostgroupName()));
            }
            if (cfg.getHostName() != null && (!cfg.getHostName().isEmpty()))
            {
                sb.append(Util.join("_", cfg.getHostName()));
            }
            sb.append("_");
            sb.append(cfg.getServiceDescription());
            return sb.toString().toLowerCase().replace(" ", "_").replace(":", "").replace(";", "").replace("[", "").replace("]", "").replace("{", "").replace("}", "");
        }
    }
}
