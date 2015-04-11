package com.intrbiz.bergamot.config;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import com.intrbiz.bergamot.util.TimeInterval;
import com.intrbiz.configuration.CfgParameter;

public class NagiosConfigConverter
{
    private boolean withDebug = false;
    
    private BergamotCfg config;

    private NagiosConfigBuilder nagiosConfig;
    
    /**
     * Host templates of services applied by host group
     */
    private Map<String, HostCfg> hostgroupTemplates = new HashMap<String, HostCfg>();

    /**
     * Short names for host templates
     */
    private AtomicInteger hostTemplateId = new AtomicInteger();
    
    /**
     * A mapping of flattened hosts to host template
     */
    private Map<String, String> hostsToHostTemplate = new HashMap<String, String>();
    
    /**
     * Host templates of services applied by host
     */
    private Map<String, HostCfg> hostTemplates = new HashMap<String, HostCfg>();
    
    /**
     * Site parameters in use we need to add
     */
    private Set<String> siteParameters = new HashSet<String>();
    
    /**
     * MacroFrame used to translate command lines
     */
    private MacroFrame macroFrame;
    
    /**
     * Map probable NRPE commands to our auto generated NRPE commands
     */
    private Map<String, String> nrpeCommands = new HashMap<String, String>();
    
    private File baseDir;

    public NagiosConfigConverter()
    {
        // setup the macro frame we will use to translate commands
        this.macroFrame = new MacroFrame();
        this.macroFrame.clearPrototypes();
        // we don't parse resources.cfg
        // map user macros to site parameters
        // assume that USER1 == plugin path
        this.macroFrame.put("USER1", "#{nagios.path}");
        for (int i = 2; i < 257; i++)
        {
            this.macroFrame.put("USER" + i, "#{nagios.user" + i + "}");
        }
        // default stuff
        this.macroFrame.put("HOSTADDRESS", "#{host.address}");
        this.macroFrame.put("HOSTNAME", "#{host.name}");
        this.macroFrame.put("SERVICEDESCRIPTION", "#{service.name}");
        // args
        for (int i = 1; i < 33; i++)
        {
            this.macroFrame.put("ARG" + i, "#{arg" + i + "}");
        }
    }
    
    public NagiosConfigConverter debugOn()
    {
        this.withDebug = true;
        return this;
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
        this.config.index(true);
        this.linkContacts();
        this.linkServices();
        this.linkHostgroups();
        this.linkServicegroups();
        // handle site parameters
        this.config.addParameter(new CfgParameter("nagios.path", null, null, "/usr/lib/nagios/plugins"));
        for (String parameter : this.siteParameters)
        {
            this.config.addParameter(new CfgParameter(parameter, null, null, ""));
        }
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
            if ("check_nrpe".equalsIgnoreCase(cfg.getCommandName()))
            {
                // override the default nagios command with a bergamot specific command
                CommandCfg command = new CommandCfg();
                command.setLoadedFrom(new File(this.baseDir, "nrpe_commands.xml"));
                command.setName("check_nrpe");
                command.setEngine("nrpe");
                command.setSummary("Check NRPE");
                command.setDescription("Check NRPE using the Bergamot NRPE worker");
                // parameters
                command.addParameter(new CfgParameter("command", "The NRPE command name", null, "#{arg1}"));
                command.addParameter(new CfgParameter("host",    "The NRPE host",         null, "#{host.address}"));
                // add
                this.config.addObject(command);
            }
            else
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
                    // extract the macros used
                    Set<String> macros = MacroProcessor.extractMacros(commandLine);
                    // add any args we found as parameters
                    // collate any site parameters we need
                    for (String macro : macros)
                    {
                        if (macro.startsWith("ARG"))
                        {
                            command.addParameter(new CfgParameter(macro.toLowerCase(), null, null, ""));        
                        }
                        else if (macro.startsWith("USER") && (! "USER1".equals(macro)))
                        {
                            this.siteParameters.add("nagios." + macro.toLowerCase());
                        }
                    }
                    // build the command line
                    // apply the macros to convert the Nagios expression to a Bergamot expression
                    commandLine = MacroProcessor.applyMacros(commandLine, this.macroFrame);
                    // add the param
                    command.addParameter(new CfgParameter("command_line", null, null, Util.coalesce(commandLine, "")));
                }
                // add
                this.config.getCommands().add(command);
            }
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
        for (NagiosServiceCfg cfg : this.nagiosConfig.getServices())
        {
            if (cfg.isRegister() != null && cfg.isRegister() == false)
            {
                // convert the service template
                this.config.getServices().add(this.convertService(cfg));
            }
            else
            {
                // build host templates
                // by host group
                if (cfg.getHostgroupName() != null)
                {
                    for (String hostgroup : cfg.getHostgroupName())
                    {
                        // get the template
                        HostCfg template = this.hostgroupTemplates.get(hostgroup);
                        if (template == null)
                        {
                            template = new HostCfg();
                            template.setName(hostgroup + "-template");
                            template.setSummary("Generic template for " + hostgroup);
                            template.setTemplate(true);
                            template.setLoadedFrom(new File(new File(this.baseDir, "templates"), "bergamot_host_" + hostgroup + "_template.xml"));
                            this.hostgroupTemplates.put(hostgroup, template);
                            this.config.addObject(template);
                        }
                        // add the service
                        template.getServices().add(this.convertService(cfg));
                    }
                }
                // by host
                if (cfg.getHostName() != null && (! cfg.getHostName().isEmpty()))
                {
                    // get a flat hosts name
                    String hosts = cfg.getHostName().stream().sorted().collect(Collectors.joining("_"));
                    // map to a short template name
                    String templateName = this.hostsToHostTemplate.get(hosts);
                    if (templateName == null)
                    {
                        templateName = "host-template-" + this.hostTemplateId.incrementAndGet();
                        this.hostsToHostTemplate.put(hosts, templateName);
                    }
                    // get the template
                    HostCfg template = this.hostTemplates.get(templateName);
                    if (template == null)
                    {
                        template = new HostCfg();
                        template.setName(templateName);
                        template.setSummary("Generic template for " + cfg.getHostName().stream().sorted().collect(Collectors.joining(", ")));
                        template.setTemplate(true);
                        template.setLoadedFrom(new File(new File(this.baseDir, "templates"), "bergamot_host_" + templateName + "_template.xml"));
                        this.hostTemplates.put(templateName, template);
                        this.config.addObject(template);
                    }
                    template.getServices().add(this.convertService(cfg));
                }
            }
        }
    }
    
    private ServiceCfg convertService(NagiosServiceCfg cfg)
    {
        // convert
        ServiceCfg service = new ServiceCfg();
        service.setLoadedFrom(convertFile(cfg.getLoadedFrom()));
        service.setTemplate(cfg.isRegister() == null || cfg.isRegister() == false ? true : null);
        service.setName(cfg.getName());
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
            service.getSchedule().setEvery(new TimeInterval(cfg.getCheckInterval(), TimeUnit.MINUTES).toString());
            service.getSchedule().setRetryEvery(new TimeInterval(cfg.getRetryInterval(), TimeUnit.MINUTES).toString());
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
        if (this.withDebug)
        {
            if (cfg.getHostgroupName() != null && (!cfg.getHostgroupName().isEmpty()))
            {
                service.addParameter(new CfgParameter("nagios.hostgroups", null, null, Util.join(", ", cfg.getHostgroupName())));
            }
            if (cfg.getHostName() != null && (!cfg.getHostName().isEmpty()))
            {
                service.addParameter(new CfgParameter("nagios.hosts", null, null, Util.join(", ", cfg.getHostName())));
            }
        }
        // check command
        if (cfg.getCheckCommand() != null)
        {
            // parse the check command
            NagiosCommandString command = NagiosCommandString.parse(cfg.getCheckCommand());
            // special case likely NRPE commands
            if ("check_nrpe".equalsIgnoreCase(command.getCommandName()) && command.getArguments().size() == 1)
            {
                String nrpeCommand = command.getArguments().get(0);
                // pull out the NRPE command and create a command definition for it
                String commandName = this.nrpeCommands.get(nrpeCommand);
                if (commandName == null)
                {
                    // command name
                    commandName = command.getCommandName() + "_" + nrpeCommand;
                    // build the command def
                    CommandCfg generatedCommand = new CommandCfg();
                    generatedCommand.setLoadedFrom(new File(this.baseDir, "nrpe_commands.xml"));
                    generatedCommand.getInheritedTemplates().add("check_nrpe");
                    generatedCommand.setName(commandName);
                    generatedCommand.setSummary("Check NRPE: " + nrpeCommand);
                    // the command parameter
                    generatedCommand.addParameter(new CfgParameter("command", null, null, nrpeCommand));
                    // add the command def
                    this.config.addObject(generatedCommand);
                    // map
                    this.nrpeCommands.put(nrpeCommand, commandName);
                }
                // use the command
                service.setCheckCommand(new CheckCommandCfg());
                service.getCheckCommand().setCommand(commandName);
            }
            else
            {
                // generic command
                service.setCheckCommand(new CheckCommandCfg());
                service.getCheckCommand().setCommand(command.getCommandName());
                // parameters
                int i = 1;
                for (String arg : command.getArguments())
                {
                    service.getCheckCommand().addParameter(new CfgParameter("arg" + i++, null, null, arg));
                }
            }
        }
        return service;
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
                host.getSchedule().setEvery(new TimeInterval(cfg.getCheckInterval(), TimeUnit.MINUTES).toString());
                host.getSchedule().setRetryEvery(new TimeInterval(cfg.getRetryInterval(), TimeUnit.MINUTES).toString());
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
            if (cfg.isRegister() == null || cfg.isRegister() == true)
            {             
                // by host group
                if (cfg.getHostgroupName() != null)
                {
                    for (String hostgroupName : cfg.getHostgroupName())
                    {
                        for (HostCfg host : this.config.getHosts())
                        {
                            if (host.containsGroup(hostgroupName))
                            {
                                host.getInheritedTemplates().add(hostgroupName + "-template");
                            }
                        }
                    }
                }
                // by host
                if (cfg.getHostName() != null && (! cfg.getHostName().isEmpty()))
                {
                    // get the template name
                    String hosts = cfg.getHostName().stream().sorted().collect(Collectors.joining("_"));
                    // map to a short template name
                    String templateName = this.hostsToHostTemplate.get(hosts);
                    // add
                    for (String hostName : cfg.getHostName())
                    {
                        HostCfg host = this.config.lookup(HostCfg.class, hostName);
                        if (host != null)
                        {
                            host.getInheritedTemplates().add(templateName);
                        }
                    }
                }
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
}
