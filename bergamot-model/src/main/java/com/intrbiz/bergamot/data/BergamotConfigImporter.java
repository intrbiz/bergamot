package com.intrbiz.bergamot.data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ActiveCheckCfg;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.CheckCfg;
import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.config.model.CommandCfg;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.bergamot.config.model.NamedObjectCfg;
import com.intrbiz.bergamot.config.model.NotificationEngineCfg;
import com.intrbiz.bergamot.config.model.NotificationsCfg;
import com.intrbiz.bergamot.config.model.PassiveCheckCfg;
import com.intrbiz.bergamot.config.model.RealCheckCfg;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.config.model.VirtualCheckCfg;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.CheckCommand;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.NotificationEngine;
import com.intrbiz.bergamot.model.Notifications;
import com.intrbiz.bergamot.model.PassiveCheck;
import com.intrbiz.bergamot.model.RealCheck;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.VirtualCheck;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.virtual.VirtualCheckOperator;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParser;
import com.intrbiz.data.DataException;

public class BergamotConfigImporter
{
    private Logger logger = Logger.getLogger(BergamotConfigImporter.class);
    
    private Site site;
    
    private BergamotCfg config;
    
    private boolean resetState = false;
    
    public BergamotConfigImporter(BergamotCfg config)
    {
        this.config = config;
    }
    
    public BergamotConfigImporter resetState(boolean resetState)
    {
        this.resetState = resetState;
        return this;
    }
    
    public void importConfiguration()
    {   
        try (BergamotDB db = BergamotDB.connect())
        {
            db.execute(()->{
                // setup the site
                this.loadSite(db);
                // load the commands
                this.loadCommands(db);
                // time periods
                this.loadTimePeriods(db);
                // teams
                this.loadTeams(db);
                // contacts
                this.loadContacts(db);
                // load the locations
                this.loadLocations(db);
                // groups
                this.loadGroups(db);
                // hosts
                this.loadHosts(db);
                // clusters
                this.loadClusters(db);
                // templates
                this.loadTemplates(db);
            });
        }
    }
    
    private void loadTemplates(BergamotDB db)
    {
        for (List<? extends TemplatedObjectCfg<?>> objects : this.config.getAllObjects())
        {
            for (TemplatedObjectCfg<?> object : objects)
            {
                if (object.getTemplateBooleanValue() && object instanceof NamedObjectCfg)
                {
                    db.setConfig(new Config(this.site.randomObjectId(), this.site.getId(), (NamedObjectCfg<?>) object));
                }
            }
        }
    }
    
    private void loadSite(BergamotDB db)
    {
        this.site = db.getSiteByName(this.config.getSite());
        if (this.site == null)
        {
            this.site = new Site();
            this.site.setId(Site.randomSiteId());
            this.site.setName(this.config.getSite());
            this.site.setSummary(this.config.getSite());
            db.setSite(site);
        }
        db.setSite(this.site);
    }
    
    private void loadCommands(BergamotDB db)
    {
        for (CommandCfg cfg : this.config.getCommands())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                logger.info("Loading command " + cfg.resolve().getName());
                // load
                Command command = db.getCommandByName(this.site.getId(), cfg.getName());
                if(command == null)
                {
                    cfg.setId(this.site.randomObjectId());
                    command = new Command();
                }
                else
                {
                    cfg.setId(command.getId());
                }
                // apply the new config
                command.configure(cfg);
                // update
                db.setCommand(command);
            }
        }
    }

    private void loadLocations(BergamotDB db)
    {
        for (LocationCfg cfg : this.config.getLocations())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                logger.info("Loading location " + cfg.resolve().getName());
                // load
                Location location = db.getLocationByName(this.site.getId(), cfg.getName());
                if (location == null)
                {
                    cfg.setId(this.site.randomObjectId());
                    location = new Location();
                }
                else
                {
                    cfg.setId(location.getId());
                }
                location.configure(cfg);
                db.setLocation(location);
            }
        }
        // link the tree
        for (LocationCfg cfg : this.config.getLocations())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                Location l = db.getLocation(cfg.getId());
                if (l != null)
                {
                    String pn = cfg.resolve().getLocation();
                    if (!Util.isEmpty(pn))
                    {
                        Location p = db.getLocationByName(this.site.getId(), pn);
                        if (p != null)
                        {
                            logger.info("Adding location " + l.getName() + " to location " + p.getName());
                            db.addLocationChild(p, l);
                        }
                    }
                }
            }
        }
    }
    
    
    private void loadGroups(BergamotDB db)
    {
        for (GroupCfg cfg : this.config.getGroups())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                logger.info("Loading group " + cfg.resolve().getName());
                // load
                Group group = db.getGroupByName(this.site.getId(), cfg.getName());
                if (group == null)
                {
                    cfg.setId(this.site.randomObjectId());
                    group = new Group();
                }
                else
                {
                    cfg.setId(group.getId());
                }
                group.configure(cfg);
                db.setGroup(group);
            }
        }
        // link the tree
        for (GroupCfg cfg : this.config.getGroups())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                Group child = db.getGroup(cfg.getId());
                if (child != null)
                {
                    for (String parentName : cfg.resolve().getGroups())
                    {
                        Group parent = db.getGroupByName(this.site.getId(), parentName);
                        if (parent != null)
                        {
                            logger.info("Adding group " + child.getName() + " to group " + parent.getName());
                            db.addGroupChild(parent, child);
                        }
                    }
                }
            }
        }
    }
    
    private void loadTimePeriods(BergamotDB db)
    {
        for (TimePeriodCfg cfg : this.config.getTimePeriods())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                logger.info("Loading time period " + cfg.resolve().getName());
                // load
                TimePeriod timePeriod = db.getTimePeriodByName(this.site.getId(), cfg.getName());
                if (timePeriod == null)
                {
                    cfg.setId(this.site.randomObjectId());
                    timePeriod = new TimePeriod();
                }
                else
                {
                    cfg.setId(timePeriod.getId());
                }
                timePeriod.configure(cfg);
                db.setTimePeriod(timePeriod);
            }
        }
        // link excludes
        for (TimePeriodCfg cfg : this.config.getTimePeriods())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                TimePeriod timePeriod = db.getTimePeriod(cfg.getId());
                if (timePeriod != null)
                {
                    for (String excludeName : cfg.resolve().getExcludes())
                    {
                        TimePeriod excluded = db.getTimePeriodByName(this.site.getId(), excludeName);
                        if (excluded != null)
                        {
                            logger.info("Adding exclude time period " + excluded.getName() + " to time period " + timePeriod.getName());
                            db.addTimePeriodExclude(timePeriod, excluded);
                        }
                    }
                }
            }
        }
    }

    private void loadTeams(BergamotDB db)
    {
        for (TeamCfg cfg : this.config.getTeams())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                logger.info("Loading team " + cfg.resolve().getName());
                // load
                Team team = db.getTeamByName(this.site.getId(), cfg.getName());
                if (team == null)
                {
                    cfg.setId(this.site.randomObjectId());
                    team = new Team();
                }
                else
                {
                    cfg.setId(team.getId());
                }
                team.configure(cfg);
                db.setTeam(team);
            }
        }
        // link the tree
        for (TeamCfg cfg : this.config.getTeams())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                Team child = db.getTeam(cfg.getId());
                if (child != null)
                {
                    for (String parentName : cfg.resolve().getTeams())
                    {
                        Team parent = db.getTeamByName(this.site.getId(), parentName);
                        if (parent != null)
                        {
                            logger.info("Adding team " + child.getName() + " to team " + parent.getName());
                            db.addTeamChild(parent, child);
                        }
                    }
                }
            }
        }
    }

    private void loadContacts(BergamotDB db)
    {
        for (ContactCfg cfg : this.config.getContacts())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                ContactCfg rcfg = cfg.resolve();
                logger.info("Loading contact " + rcfg.getName());
                // load
                Contact contact = db.getContactByName(this.site.getId(), rcfg.getName());
                if (contact == null)
                {
                    cfg.setId(this.site.randomObjectId());
                    contact = new Contact();
                    // set a default password
                    contact.hashPassword("bergamot");
                }
                else
                {
                    cfg.setId(contact.getId());
                }
                contact.configure(cfg);
                // notifications
                this.loadNotifications(contact.getId(), rcfg.getNotifications(), db);
                // store
                db.setContact(contact);
                // teams
                for (String teamName : rcfg.getTeams())
                {
                    Team team = db.getTeamByName(this.site.getId(), teamName);
                    if (team != null)
                    {
                        logger.info("Adding contact " + contact.getName() + " to team " + team.getName());
                        team.addContact(contact);
                    }
                }
            }
        }
    }
    
    private void loadNotifications(UUID owner, NotificationsCfg cfg, BergamotDB db)
    {
        Notifications notifications = new Notifications();
        notifications.setId(owner);
        notifications.setEnabled(cfg.getEnabledBooleanValue());
        notifications.setAlertsEnabled(cfg.getAlertsBooleanValue());
        notifications.setRecoveryEnabled(cfg.getRecoveryBooleanValue());
        notifications.setIgnore(cfg.getIgnore().stream().map((e) -> {return Status.valueOf(e.toUpperCase());}).collect(Collectors.toList()));
        notifications.setAllEnginesEnabled(cfg.getAllEnginesEnabledBooleanValue());
        // load the time period
        if (! Util.isEmpty(cfg.getNotificationPeriod()))
        {
            TimePeriod timePeriod = db.getTimePeriodByName(this.site.getId(), cfg.getNotificationPeriod());
            if (timePeriod != null)
            {
                notifications.setTimePeriodId(timePeriod.getId());
            }
        }
        db.setNotifications(notifications);
        // engines
        for (NotificationEngineCfg ecfg : cfg.getNotificationEngines())
        {
            NotificationEngine notificationEngine = new NotificationEngine();
            notificationEngine.setNotificationsId(notifications.getId());
            notificationEngine.setEngine(ecfg.getEngine());
            notificationEngine.setEnabled(ecfg.getEnabledBooleanValue());
            notificationEngine.setAlertsEnabled(ecfg.getAlertsBooleanValue());
            notificationEngine.setRecoveryEnabled(ecfg.getRecoveryBooleanValue());
            notificationEngine.setIgnore(ecfg.getIgnore().stream().map((e) -> {return Status.valueOf(e.toUpperCase()); }).collect(Collectors.toList()));
            if (! Util.isEmpty(ecfg.getNotificationPeriod()))
            {
                TimePeriod timePeriod = db.getTimePeriodByName(this.site.getId(), ecfg.getNotificationPeriod());
                if (timePeriod != null)
                {
                    notificationEngine.setTimePeriodId(timePeriod.getId());
                }
            }
            //
            db.setNotificationEngine(notificationEngine);
        }
    }
    
    

    private void loadHosts(BergamotDB db)
    {
        for (HostCfg cfg : this.config.getHosts())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                // resolved config
                HostCfg rcfg = cfg.resolve();
                logger.info("Loading host " + rcfg.getName());
                // load
                Host host = db.getHostByName(this.site.getId(), rcfg.getName());
                if (host == null)
                {
                    cfg.setId(this.site.randomObjectId());
                    host = new Host();
                }
                else
                {
                    cfg.setId(host.getId());
                }
                host.configure(cfg);
                // load the check details
                this.loadActiveCheck(host, rcfg, db);
                // add locations
                host.setLocationId(null);
                String locationName = rcfg.getLocation();
                if (!Util.isEmpty(locationName))
                {
                    Location location = db.getLocationByName(this.site.getId(), locationName);
                    if (location != null)
                    {
                        host.setLocationId(location.getId());
                    }
                }
                // add the host
                db.setHost(host);
                // add services
                for (ServiceCfg scfg : rcfg.getServices())
                {
                    this.loadService(host, scfg, db);
                }
                // add traps
                for (TrapCfg tcfg : rcfg.getTraps())
                {
                    this.loadTrap(host, tcfg, db);
                }
            }
        }
    }
    
    private void loadActiveCheck(ActiveCheck<?,?> check, ActiveCheckCfg<?> rcfg, BergamotDB db)
    {
        this.loadRealCheck(check, rcfg, db);
        // the check period
        if (! Util.isEmpty(rcfg.getSchedule().getTimePeriod()))
        {
            TimePeriod timePeriod = db.getTimePeriodByName(this.site.getId(), rcfg.getSchedule().getTimePeriod());
            if (timePeriod != null)
            {
                check.setTimePeriodId(timePeriod.getId());
            }
        }        
    }
    
    private void loadCheckState(Check<?,?> check, CheckCfg<?> cfg, BergamotDB db)
    {
        CheckState state = db.getCheckState(check.getId());
        if (state == null || this.resetState)
        {
            state = new CheckState();
            state.setCheckId(check.getId());
            state.configure(cfg);
            db.setCheckState(state);
        }
    }
    
    private void loadRealCheck(RealCheck<?,?> check, RealCheckCfg<?> rcfg, BergamotDB db)
    {
        this.loadCheck(check, rcfg, db);
        // the check command
        if (rcfg.getCheckCommand() != null)
        {
            // lookup the command
            Command command = db.getCommandByName(this.site.getId(), rcfg.getCheckCommand().getCommand());
            if (command != null)
            {
                CheckCommand checkCommand = new CheckCommand();
                checkCommand.setCheckId(check.getId());
                checkCommand.configure(rcfg.getCheckCommand());
                checkCommand.setCommandId(command.getId());
                db.setCheckCommand(checkCommand);
                logger.info("Added command " + command.getName() + " to check " + check.getName());
            }
            else
            {
                throw new DataException("The command " + rcfg.getCheckCommand().getCommand() + " could not be found, needed by " + check.getName());
            }
        }
    }

    private void loadCheck(Check<?,?> check, CheckCfg<?> rcfg, BergamotDB db)
    {
        // set the processing pool
        check.setPool(this.site.computeProcessingPool(check.getId()));
        // the state
        this.loadCheckState(check, rcfg, db);
        // notifications
        this.loadNotifications(check.getId(), rcfg.getNotifications(), db);
        // notify
        check.getTeamIds().clear();
        check.getContactIds().clear();
        for (String teamName : rcfg.getNotify().getTeams())
        {
            Team team = db.getTeamByName(this.site.getId(), teamName);
            if (team != null)
            {
                check.getTeamIds().add(team.getId());
            }
        }
        for (String contactName : rcfg.getNotify().getContacts())
        {
            Contact contact = db.getContactByName(this.site.getId(), contactName);
            if (contact != null)
            {
                check.getContactIds().add(contact.getId());
            }
        }
        // the groups
        check.getGroupIds().clear();
        for (String groupName : rcfg.getGroups())
        {
            Group group = db.getGroupByName(this.site.getId(), groupName);
            if (group != null)
            {
                logger.info("Adding check " + check.getName() + " to group " + group.getName());
                check.getGroupIds().add(group.getId());
            }
        }
    }

    private void loadService(Host host, ServiceCfg cfg, BergamotDB db)
    {
        // resolve
        ServiceCfg rcfg = cfg.resolve();
        logger.info("Adding service " + rcfg.getName() + " to host " + host.getName());
        // create the service
        Service service = db.getServiceOnHost(host.getId(), rcfg.getName());
        if (service == null)
        {
            cfg.setId(this.site.randomObjectId());
            service = new Service();
            service.setHostId(host.getId());
        }
        else
        {
            cfg.setId(service.getId());
        }
        service.configure(cfg);
        // load the check details
        this.loadActiveCheck(service, rcfg, db);
        // add
        db.setService(service);
    }
    
    
    
    private void loadTrap(Host host, TrapCfg cfg, BergamotDB db)
    {
        // resolve
        TrapCfg rcfg = cfg.resolve();
        logger.info("Adding trap " + rcfg.getName() + " to host " + host.getName());
        // create the service
        Trap trap = db.getTrapOnHost(host.getId(), cfg.getName());
        if (trap == null)
        {
            cfg.setId(this.site.randomObjectId());
            trap = new Trap();
            trap.setHostId(host.getId());
        }
        else
        {
            cfg.setId(trap.getId());
        }
        trap.configure(cfg);
        // load the check details
        this.loadPasiveCheck(trap, rcfg, db);
        // add
        db.setTrap(trap);
    }
    
    private void loadPasiveCheck(PassiveCheck<?,?> check, PassiveCheckCfg<?> rcfg, BergamotDB db)
    {
        this.loadRealCheck(check, rcfg, db);
    }
    
    private void loadVirtualCheck(VirtualCheck<?,?> check, VirtualCheckCfg<?> rcfg, BergamotDB db)
    {
        this.loadCheck(check, rcfg, db);
        // parse the condition
        if (! Util.isEmpty(rcfg.getCondition()))
        {
            VirtualCheckOperator cond = VirtualCheckExpressionParser.parseVirtualCheckExpression(db.createVirtualCheckContext(this.site.getId()), rcfg.getCondition());
            if (cond != null)
            {
                check.setCondition(cond);
                logger.info("Using virtual check condition " + cond.toString() + " for " + check);
                // cross reference the checks
                check.setReferenceIds(cond.computeDependencies().stream().map(Check::getId).collect(Collectors.toList()));
            }
        }
    }

    private void loadClusters(BergamotDB db)
    {
        for (ClusterCfg cfg : this.config.getClusters())
        {
            if (!cfg.getTemplateBooleanValue())
            {
                // resolved config
                ClusterCfg rcfg = cfg.resolve();
                logger.info("Loading cluster " + rcfg.getName());
                // load
                Cluster cluster = db.getClusterByName(this.site.getId(), cfg.getName());
                if (cluster == null)
                {
                    cfg.setId(this.site.randomObjectId());
                    cluster = new Cluster();
                }
                else
                {
                    cfg.setId(cluster.getId());
                }
                cluster.configure(cfg);
                // load the check details
                this.loadVirtualCheck(cluster, rcfg, db);
                // add the cluster
                db.setCluster(cluster);
                // add resources
                for (ResourceCfg scfg : rcfg.getResources())
                {
                    this.loadResource(cluster, scfg, db);
                }
            }
        }
    }
    
    private void loadResource(Cluster cluster, ResourceCfg cfg, BergamotDB db)
    {
        // resolve
        ResourceCfg rcfg = cfg.resolve();
        logger.info("Adding resource " + rcfg.getName() + " to cluster " + cluster.getName());
        // create the service
        Resource resource = db.getResourceOnCluster(cluster.getId(), rcfg.getName());
        if (resource == null)
        {
            cfg.setId(this.site.randomObjectId());
            resource = new Resource();
            resource.setClusterId(cluster.getId());
        }
        else
        {
            cfg.setId(resource.getId());
        }
        resource.configure(cfg);
        // load the check details
        this.loadVirtualCheck(resource, rcfg, db);
        // add
        db.setResource(resource);
    }
}
