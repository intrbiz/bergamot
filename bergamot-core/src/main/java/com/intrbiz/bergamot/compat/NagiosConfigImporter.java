package com.intrbiz.bergamot.compat;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.compat.command.NagiosCommandString;
import com.intrbiz.bergamot.compat.config.builder.NagiosConfigBuilder;
import com.intrbiz.bergamot.compat.config.model.CommandCfg;
import com.intrbiz.bergamot.compat.config.model.HostCfg;
import com.intrbiz.bergamot.compat.config.model.HostgroupCfg;
import com.intrbiz.bergamot.compat.config.model.LocationCfg;
import com.intrbiz.bergamot.compat.config.model.ServiceCfg;
import com.intrbiz.bergamot.compat.config.model.ServicegroupCfg;
import com.intrbiz.bergamot.compat.config.model.TimeperiodCfg;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.CheckCommand;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.HostGroup;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.ServiceGroup;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.timeperiod.TimeRange;
import com.intrbiz.bergamot.store.ObjectStore;
import com.intrbiz.bergamot.timerange.TimeRangeParser;

/**
 * Import a parsed Nagios configuration into an ObjectStore.
 * 
 * This will interpret the Nagios configuration and create Host, Service, etc object and link them together.
 * 
 */
public class NagiosConfigImporter
{
    private Logger logger = Logger.getLogger(NagiosConfigImporter.class);

    private final NagiosConfigBuilder nagiosConfig;

    public NagiosConfigImporter(NagiosConfigBuilder nagiosConfig)
    {
        this.nagiosConfig = nagiosConfig;
    }

    public NagiosConfigBuilder getNagiosConfig()
    {
        return nagiosConfig;
    }

    // build the object store from the configuration

    /**
     * Compute the configuration into a graph of model objects using the default object store
     */
    public ObjectStore compute()
    {
        ObjectStore store = new ObjectStore();
        this.compute(store);
        return store;
    }

    /**
     * Compute the configuration into a graph of model objects
     */
    public void compute(ObjectStore store)
    {
        // load commands
        this.loadCommands(store);
        // load the time periods
        this.loadTimePeriods(store);
        this.loadTimePeriodExcludes(store);
        // load all host groups
        this.loadHostgroups(store);
        // load all service groups
        this.loadServicegroups(store);
        // load locations
        this.loadLocations(store);
        this.linkLocations(store);
        // load all host objects
        this.loadHosts(store);
        this.linkHostgroups(store);
        // load all service objects
        this.loadServices(store);
        // TODO build servicegroup members
    }

    private void loadLocations(ObjectStore store)
    {
        for (LocationCfg cfg : this.nagiosConfig.getLocations())
        {
            if (cfg.isRegister())
            {
                if (!store.containsLocation(cfg.getLocationName()))
                {
                    // add the host
                    Location location = new Location();
                    location.configure(cfg);
                    store.addLocation(location);
                    logger.trace("Adding location " + location.getName());
                }
                else
                {
                    logger.warn("The location " + cfg.getLocationName() + " is already defined!");
                }
            }
            else
            {
                logger.trace("Not registering location: " + cfg.getName());
            }
        }
    }

    private void linkLocations(ObjectStore store)
    {
        for (LocationCfg locationCfg : this.nagiosConfig.getLocations())
        {
            if (locationCfg.isRegister())
            {
                String parent = locationCfg.resolveLocation();
                if (! Util.isEmpty(parent))
                {
                    Location location = store.lookupLocation(locationCfg.getLocationName());
                    if (location != null)
                    {
                        Location parentLocation = store.lookupLocation(parent);
                        if (parentLocation != null)
                        {
                            parentLocation.addLocation(location);
                        }
                        else
                        {
                            logger.warn("The location " + parent + " does not exist!");
                        }
                    }
                }
            }
        }
    }

    private void loadHosts(ObjectStore store)
    {
        for (HostCfg cfg : this.nagiosConfig.getHosts())
        {
            if (cfg.isRegister())
            {
                if (!store.containsHost(cfg.getHostName()))
                {
                    // add the host
                    Host host = new Host();
                    host.configure(cfg);
                    store.addHost(host);
                    // location
                    String locationName = cfg.resolveLocation();
                    if (!Util.isEmpty(locationName))
                    {
                        Location location = store.lookupLocation(locationName);
                        if (location != null)
                        {
                            location.addHost(host);
                        }
                        else
                        {
                            logger.warn("The location " + locationName + " does not exist!");
                        }
                    }
                    // register into host groups
                    for (String hostgroupName : cfg.getHostgroups())
                    {
                        HostGroup hostGroup = store.lookupHostgroup(hostgroupName);
                        if (hostGroup != null)
                        {
                            hostGroup.addHost(host);
                        }
                        else
                        {
                            logger.warn("The hostgroup " + hostgroupName + " does not exist!");
                        }
                    }
                    // the command
                    this.loadCheckCommand(cfg.resolveCheckCommand(), host, store);
                    // the time period
                    this.loadCheckPeriod(cfg.resolveCheckPeriod(), host, store);
                    logger.trace("Adding host " + host);
                }
                else
                {
                    logger.warn("The host " + cfg.getHostName() + " is already defined!");
                }
            }
            else
            {
                logger.trace("Not registering host: " + cfg.getName());
            }
        }
    }

    private void loadHostgroups(ObjectStore store)
    {
        for (HostgroupCfg cfg : this.nagiosConfig.getHostgroups())
        {
            if (cfg.isRegister())
            {
                if (!store.containsHostgroup(cfg.getHostgroupName()))
                {
                    // add the host
                    HostGroup hg = new HostGroup();
                    hg.configure(cfg);
                    store.addHostgroup(hg);
                    logger.trace("Adding hostgroup " + hg.getName());
                }
                else
                {
                    logger.warn("The hostgroup " + cfg.getHostgroupName() + " is already defined!");
                }
            }
            else
            {
                logger.trace("Not registering hostgroup: " + cfg.getName());
            }
        }
    }
    
    private void linkHostgroups(ObjectStore store)
    {
        for (HostgroupCfg cfg : this.nagiosConfig.getHostgroups())
        {
            if (cfg.isRegister())
            {
                HostGroup hostGroup = store.lookupHostgroup(cfg.getHostgroupName());
                if (hostGroup != null)
                {
                    // hosts
                    List<String> members = cfg.resolveMembers();
                    if (members != null)
                    {
                        for (String member : members)
                        {
                            Host host = store.lookupHost(member);
                            if (host != null)
                            {
                                hostGroup.addHost(host);
                            }
                            else
                            {
                                logger.warn("The host " + member + " does not exist, cannot add it to the hostgroup " + hostGroup.getName());
                            }
                        }
                    }
                    // host groups
                    List<String> hostgroups = cfg.resolveMembers();
                    if (hostgroups != null)
                    {
                        for (String member : hostgroups)
                        {
                            HostGroup memberHostGroup = store.lookupHostgroup(member);
                            if (member != null)
                            {
                                hostGroup.addMember(memberHostGroup);
                            }
                            else
                            {
                                logger.warn("The hostgroup " + member + " does not exist, cannot add it to the hostgroup " + hostGroup.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadServices(ObjectStore store)
    {
        for (ServiceCfg cfg : this.nagiosConfig.getServices())
        {
            if (cfg.isRegister())
            {
                List<Host> hosts = new LinkedList<Host>();
                // find the hosts to add the service on
                for (String hostName : cfg.resolveHostName())
                {
                    Host host = store.lookupHost(hostName);
                    if (host != null)
                    {
                        hosts.add(host);
                    }
                    else
                    {
                        logger.warn("The host " + hostName + " does not exist");
                    }
                }
                for (String hostgroupName : cfg.resolveHostgroupName())
                {
                    HostGroup hostGroup = store.lookupHostgroup(hostgroupName);
                    if (hostGroup != null)
                    {
                        hosts.addAll(hostGroup.getHosts());
                    }
                    else
                    {
                        logger.warn("The hostgroup " + hostgroupName + " does not exist");
                    }
                }
                // add the service
                for (Host host : hosts)
                {
                    if (host.containsService(cfg.getServiceDescription()))
                    {
                        logger.warn("The service " + cfg.getServiceDescription() + " is already defined on the host " + host.getName() + "!");
                    }
                    else
                    {
                        Service service = new Service();
                        service.configure(cfg);
                        host.addService(service);
                        store.addService(service);
                        // register into service groups
                        for (String servicegroupName : cfg.getServicegroups())
                        {
                            ServiceGroup sg = store.lookupServicegroup(servicegroupName);
                            if (sg != null)
                            {
                                sg.addService(service);
                            }
                            else
                            {
                                logger.warn("The servicegroup " + servicegroupName + " does not exist!");
                            }
                        }
                        // the command
                        this.loadCheckCommand(cfg.resolveCheckCommand(), service, store);
                        // the time period
                        this.loadCheckPeriod(cfg.resolveCheckPeriod(), service, store);
                        logger.trace("Adding service " + service);
                    }
                }
            }
            else
            {
                logger.trace("Not registering service: " + cfg.getName());
            }
        }
    }

    private void loadCheckPeriod(String checkPeriod, Check on, ObjectStore store)
    {
        if (checkPeriod != null)
        {
            TimePeriod timePeriod = store.lookupTimePeriod(checkPeriod);
            if (timePeriod != null)
            {
                timePeriod.addCheck(on);
            }
            else
            {
                logger.warn("The timeperiod " + checkPeriod + " is not defined, used by " + on);
            }
        }
    }

    private void loadCheckCommand(String checkCommand, Check on, ObjectStore store)
    {
        // the command
        NagiosCommandString parsedCommand = NagiosCommandString.parse(checkCommand);
        if (parsedCommand != null)
        {
            logger.trace("Parsed command: " + parsedCommand.toString());
            Command command = store.lookupCommand(parsedCommand.getCommandName());
            if (command != null)
            {
                CheckCommand theCheck = new CheckCommand();
                theCheck.setCommand(command);
                // add the ARG* parameters
                int argIdx = 1;
                for (String argument : parsedCommand.getArguments())
                {
                    theCheck.addParameter("ARG" + (argIdx++), argument);
                }
                on.setCheckCommand(theCheck);
                logger.trace("Added command " + command.getName() + " for (" + on + ") with " + theCheck);
            }
            else
            {
                logger.warn("The command " + parsedCommand.getCommandName() + " does not exist!");
            }
        }
        else
        {
            logger.trace("The " + on + " does not have a command defined!");
        }
    }

    private void loadServicegroups(ObjectStore store)
    {
        for (ServicegroupCfg cfg : this.nagiosConfig.getServicegroups())
        {
            if (cfg.isRegister())
            {
                if (!store.containsServicegroup(cfg.getServicegroupName()))
                {
                    // add the host
                    ServiceGroup sg = new ServiceGroup();
                    sg.configure(cfg);
                    store.addServicegroup(sg);
                    logger.trace("Adding servicegroup " + sg.getName());
                }
                else
                {
                    logger.warn("The servicegroup " + cfg.getServicegroupName() + " is already defined!");
                }
            }
            else
            {
                logger.trace("Not registering servicegroup: " + cfg.getName());
            }
        }
    }

    private void loadCommands(ObjectStore store)
    {
        for (CommandCfg cfg : this.nagiosConfig.getCommands())
        {
            if (cfg.isRegister())
            { 
                if (!store.containsCommand(cfg.getCommandName()))
                {
                    // add the host
                    Command c = new Command();
                    c.configure(cfg);
                    store.addCommand(c);
                    logger.trace("Adding command " + c.getName());
                }
                else
                {
                    logger.warn("The command " + cfg.getCommandName() + " is already defined!");
                }
            }
            else
            {
                logger.trace("Not registering command: " + cfg.getName());
            }
        }
    }

    private void loadTimePeriods(ObjectStore store)
    {
        for (TimeperiodCfg cfg : this.nagiosConfig.getTimeperiods())
        {
            if (cfg.isRegister())
            {
                TimePeriod timePeriod = new TimePeriod();
                timePeriod.configure(cfg);
                store.addTimePeriod(timePeriod);
                if (cfg.resolvePeriods() != null)
                {
                    for (String period : cfg.resolvePeriods())
                    {
                        try
                        {
                            TimeRange range = TimeRangeParser.parseTimeRange(period);
                            timePeriod.addRange(range);
                        }
                        catch (Exception e)
                        {
                            logger.error("Failed to parse time range " + period + " on timeperiod " + timePeriod.getName() + " ignoring time range");
                        }
                    }
                }
                else
                {
                    logger.warn("The timeperiod " + timePeriod.getName() + " has no periods defined, as such it will operate at 24x7");
                }
            }
            else
            {
                logger.trace("Not registering timeperiod: " + cfg.getName());
            }
        }
    }

    private void loadTimePeriodExcludes(ObjectStore store)
    {
        for (TimeperiodCfg cfg : this.nagiosConfig.getTimeperiods())
        {
            if (cfg.isRegister())
            {
                List<String> excludes = cfg.resolveExclude();
                if (excludes != null)
                {
                    TimePeriod timePeriod = store.lookupTimePeriod(cfg.getTimeperiodName());
                    if (timePeriod != null)
                    {
                        for (String exclude : excludes)
                        {
                            TimePeriod excluded = store.lookupTimePeriod(exclude);
                            if (excluded != null)
                            {
                                timePeriod.addExclude(excluded);
                            }
                            else
                            {
                                logger.warn("The timeperiod " + exclude + " is not defined, it is an exclude of timeperiod " + timePeriod.getName());
                            }
                        }
                    }
                }
            }
        }
    }
}
