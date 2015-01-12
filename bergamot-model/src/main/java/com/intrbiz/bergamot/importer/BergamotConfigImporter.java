package com.intrbiz.bergamot.importer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg.ObjectState;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.config.model.VirtualCheckCfg;
import com.intrbiz.bergamot.config.validator.ValidatedBergamotConfiguration;
import com.intrbiz.bergamot.data.BergamotDB;
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
import com.intrbiz.bergamot.model.message.scheduler.EnableCheck;
import com.intrbiz.bergamot.model.message.scheduler.RescheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.ScheduleCheck;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.virtual.VirtualCheckOperator;
import com.intrbiz.bergamot.queue.SchedulerQueue;
import com.intrbiz.bergamot.queue.key.SchedulerKey;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParser;
import com.intrbiz.configuration.Configuration;
import com.intrbiz.data.DataException;
import com.intrbiz.queue.RoutedProducer;

public class BergamotConfigImporter
{    
    private BergamotImportReport  report;
    
    private Site site;
    
    private BergamotCfg config;
    
    private boolean resetState = false;
    
    private boolean createSite = false;
    
    private boolean online = false;
    
    private Map<String, CascadedChange> cascadedChanges = new HashMap<String, CascadedChange>();
    
    private Set<String> loadedObjects = new HashSet<String>();
    
    private List<DelayedSchedulerAction> delayedSchedulerActions = new LinkedList<DelayedSchedulerAction>();
    
    public BergamotConfigImporter(ValidatedBergamotConfiguration validated)
    {
        if (! validated.getReport().isValid()) throw new RuntimeException("Cannot import invalid configuration");
        this.config = validated.getConfig();
    }
    
    public BergamotConfigImporter resetState(boolean resetState)
    {
        this.resetState = resetState;
        return this;
    }
    
    public BergamotConfigImporter createSite(boolean createSite)
    {
        this.createSite = createSite;
        return this;
    }
    
    public BergamotConfigImporter online(boolean online)
    {
        this.online = online;
        return this;
    }
    
    public BergamotImportReport importConfiguration()
    {
        if (this.report == null)
        {
            this.report = new BergamotImportReport(this.config.getSite());
            try
            {
                // update database
                try (BergamotDB db = BergamotDB.connect())
                {
                    db.execute(()-> {
                        // setup the site
                        this.loadSite(db);
                        // templates
                        this.loadTemplates(db);
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
                    });
                    // to avoid cache concurrency issues, clear the entire cache
                    // we'll improve this in later releases
                    this.report.info("Clearing all caches");
                    db.cacheClear();
                }
                // we must publish any scheduling changes after we have committed the transaction
                // publish all scheduling changes
                if (this.online)
                {
                    try (SchedulerQueue queue = SchedulerQueue.open())
                    {
                        try (RoutedProducer<SchedulerAction> producer = queue.publishSchedulerActions())
                        {
                            for (DelayedSchedulerAction delayedAction : this.delayedSchedulerActions)
                            {
                                this.report.info("Publishing scheduler action: " + delayedAction.change + " for check " + delayedAction.check.getClass().getSimpleName() + ":" + delayedAction.check.getName());
                                // apply the scheduling change
                                if (delayedAction.change == DelayedSchedulerAction.SchedulingChange.SCHEDULE)
                                {
                                    producer.publish(new SchedulerKey(delayedAction.check.getSiteId(), delayedAction.check.getPool()), new ScheduleCheck(delayedAction.check.toStubMO()));
                                }
                                else if (delayedAction.change == DelayedSchedulerAction.SchedulingChange.RESCHEDULE)
                                {
                                    producer.publish(new SchedulerKey(delayedAction.check.getSiteId(), delayedAction.check.getPool()), new RescheduleCheck(delayedAction.check.toStubMO()));
                                }
                                // ensure the check is enabled
                                producer.publish(new SchedulerKey(delayedAction.check.getSiteId(), delayedAction.check.getPool()), new EnableCheck(delayedAction.check.toStubMO()));
                            }
                        }
                    }
                }
            }
            catch (Throwable e)
            {
                Logger.getLogger(BergamotConfigImporter.class).error("Failed to import configuration", e);
                this.report.error("Configuration change aborted due to unhandled error: " + e.getMessage());
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                this.report.error(sw.toString());
            }
        }
        return this.report;
    }
    
    private void loadSite(BergamotDB db)
    {
        this.site = db.getSiteByName(this.config.getSite());
        if (this.site == null)
        {
            if (this.createSite)
            {
                this.site = new Site();
                this.site.setId(Site.randomSiteId());
                this.site.setName(this.config.getSite());
                this.site.setSummary(this.config.getSite());
                db.setSite(site);
            }
            else
            {
                this.report.error("Site '" + this.config.getSite() + "' does not exist and cannot be created, aborting!");
                throw new RuntimeException("Site '" + this.config.getSite() + "' does not exist and cannot be created, aborting!");
            }
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
                    String type = Configuration.getRootElement(((NamedObjectCfg<?>) object).getClass());
                    //
                    Config conf = db.getConfigByName(this.site.getId(), type, object.getName());
                    if (conf == null)
                    {
                        conf =  new Config(this.site.randomObjectId(), this.site.getId(), (NamedObjectCfg<?>) object);
                        this.report.info("Configuring new " + type + " template: " + object.getName());
                    }
                    else
                    {
                        if (ObjectState.isRemove(object.getObjectState()))
                        {
                            // TODO: we currently don't cope with deleting templated
                            throw new RuntimeException("Removing template is not currently supported!");
                        }
                        else
                        {
                            conf.fromConfiguration((NamedObjectCfg<?>) object);
                            this.report.info("Reconfiguring existing " + type + " template: " + object.getName() + " (" + conf.getId() + ")");
                            // cascade - note this recursively queries for all objects affected by a change to this template
                            for (Config config : conf.listAllDependentsObjects())
                            {
                                this.report.info("  Change cascades to: " + config.getType() + ":" + config.getName());
                                this.cascadedChanges.put(config.getQualifiedName(), new CascadedChange(object, conf, config));
                            }
                        }
                    }
                    db.setConfig(conf);
                }
            }
        }
    }
    
    private void loadCommands(BergamotDB db)
    {
        for (CommandCfg cfg : this.config.getCommands())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                if (ObjectState.isRemove(cfg.getObjectState()))
                {
                    // remove the command
                    this.removeCommand(cfg, db);
                }
                else
                {
                    // add or change the command
                    this.loadCommand(cfg, db);
                }
            }
        }
        // load any commands where a template change cascades
        for (CascadedChange change : this.cascadedChanges.values())
        {
            if (change.dependent.getConfiguration() instanceof CommandCfg)
            {
                CommandCfg cfg = (CommandCfg) change.dependent.getConfiguration();
                if (! (cfg.getTemplateBooleanValue() || this.loadedObjects.contains("command:" + cfg.getName())))
                {
                    this.report.info("Reconfiguring command " + cfg.getName() + " due to a change to the " + change.template.getName() + " inherited template.");
                    // first we need to resolve the inheritance for the cascaded object
                    db.getConfigResolver(this.site.getId()).resolveInherit(cfg);
                    // load
                    this.loadCommand(cfg, db);
                }
            }
        }
    }
    
    private void removeCommand(CommandCfg cfg, BergamotDB db)
    {
        this.report.info("Removing command: " + cfg.resolve().getName());
        // remove the command
        Command command = db.getCommandByName(this.site.getId(), cfg.getName());
        if (command != null)
        {
            // TODO: remove should cascade and remove and CheckCommands which 
            //       are using this command
            db.removeCommand(command.getId());
        }
    }
    
    private void loadCommand(CommandCfg cfg, BergamotDB db)
    {
        if (this.loadedObjects.contains("command:" + cfg.getName()))
        {
            this.report.info("Skipping reconfiguring command " + cfg.getName());
            return;
        }
        // load
        Command command = db.getCommandByName(this.site.getId(), cfg.getName());
        if(command == null)
        {
            cfg.setId(this.site.randomObjectId());
            command = new Command();
            this.report.info("Configuring new command: " + cfg.resolve().getName());
        }
        else
        {
            cfg.setId(command.getId());
            this.report.info("Reconfiguring existing command: " + cfg.resolve().getName() + " (" + cfg.getId() + ")");
        }
        // apply the new config
        command.configure(cfg);
        // update
        db.setCommand(command);
        this.loadedObjects.add("command:" + cfg.getName());
    }

    private void loadLocations(BergamotDB db)
    {
        for (LocationCfg cfg : this.config.getLocations())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                if (ObjectState.isRemove(cfg.getObjectState()))
                {
                    // remove the location
                    this.removeLocation(cfg, db);
                }
                else
                {
                    // add or change the location
                    this.loadLocation(cfg, db);
                }
            }
        }
        // link the tree
        for (LocationCfg cfg : this.config.getLocations())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                if (ObjectState.isChange(cfg.getObjectState()))
                {
                    // only link locations that are being changed or added
                    this.linkLocation(cfg, db);
                }
            }
        }
        // load any location where a template change cascades
        for (CascadedChange change : this.cascadedChanges.values())
        {
            if (change.dependent.getConfiguration() instanceof LocationCfg)
            {
                LocationCfg cfg = (LocationCfg) change.dependent.getConfiguration();
                if (! (cfg.getTemplateBooleanValue() || this.loadedObjects.contains("location:" + cfg.getName())))
                {
                    this.report.info("Reconfiguring location " + cfg.getName() + " due to a change to the " + change.template.getName() + " inherited template.");
                    // first we need to resolve the inheritance for the cascaded object
                    db.getConfigResolver(this.site.getId()).resolveInherit(cfg);
                    // load
                    this.loadLocation(cfg, db);
                    this.linkLocation(cfg, db);
                }
            }
        }
    }
    
    private void removeLocation(LocationCfg cfg, BergamotDB db)
    {
        this.report.info("Removing location: " + cfg.resolve().getName());
        // remove the location
        Location location = db.getLocationByName(this.site.getId(), cfg.getName());
        if (location != null)
        {
            // TODO: remove location should delink any child locations
            db.removeLocation(location.getId());
        }
    }
    
    private void loadLocation(LocationCfg cfg, BergamotDB db)
    {
        if (this.loadedObjects.contains("timeperiod:" + cfg.getName()))
        {
            this.report.info("Skipping reconfiguring timeperiod " + cfg.getName());
            return;
        }
        // load
        Location location = db.getLocationByName(this.site.getId(), cfg.getName());
        if (location == null)
        {
            cfg.setId(this.site.randomObjectId());
            location = new Location();
            this.report.info("Configuring new location: " + cfg.resolve().getName());
        }
        else
        {
            cfg.setId(location.getId());
            this.report.info("Reconfiguring existing location: " + cfg.resolve().getName() + " (" + cfg.getId() + ")");
        }
        location.configure(cfg);
        db.setLocation(location);
        this.loadedObjects.add("location:" + cfg.getName());
    }
    
    private void linkLocation(LocationCfg cfg, BergamotDB db)
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
                    this.report.info("Adding location " + l.getName() + " to location " + p.getName());
                    db.addLocationChild(p, l);
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
                if (ObjectState.isRemove(cfg.getObjectState()))
                {
                    this.removeGroup(cfg, db);
                }
                else
                {
                    this.loadGroup(cfg, db);
                }
            }
        }
        // link the tree
        for (GroupCfg cfg : this.config.getGroups())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                if (ObjectState.isChange(cfg.getObjectState()))
                {
                    this.linkGroup(cfg, db);
                }
            }
        }
        // load any group where a template change cascades
        for (CascadedChange change : this.cascadedChanges.values())
        {
            if (change.dependent.getConfiguration() instanceof GroupCfg)
            {
                GroupCfg cfg = (GroupCfg) change.dependent.getConfiguration();
                if (! (cfg.getTemplateBooleanValue() || this.loadedObjects.contains("group:" + cfg.getName())))
                {
                    this.report.info("Reconfiguring group " + cfg.getName() + " due to a change to the " + change.template.getName() + " inherited template.");
                    // first we need to resolve the inheritance for the cascaded object
                    db.getConfigResolver(this.site.getId()).resolveInherit(cfg);
                    // load
                    this.loadGroup(cfg, db);
                    this.linkGroup(cfg, db);
                }
            }
        }
    }
    
    private void removeGroup(GroupCfg cfg, BergamotDB db)
    {
        this.report.info("Removing group: " + cfg.resolve().getName());
        // remove the group
        Group group = db.getGroupByName(this.site.getId(), cfg.getName());
        if (group != null)
        {
            // TODO: remove group should delink any child groups
            db.removeGroup(group.getId());
        }
    }
    
    private void loadGroup(GroupCfg cfg, BergamotDB db)
    {
        if (this.loadedObjects.contains("group:" + cfg.getName()))
        {
            this.report.info("Skipping reconfiguring group " + cfg.getName());
            return;
        }
        // load
        Group group = db.getGroupByName(this.site.getId(), cfg.getName());
        if (group == null)
        {
            cfg.setId(this.site.randomObjectId());
            group = new Group();
            this.report.info("Configuring new group: " + cfg.resolve().getName());
        }
        else
        {
            cfg.setId(group.getId());
            this.report.info("Reconfiguring existing group: " + cfg.resolve().getName() + " (" + cfg.getId() + ")");
        }
        group.configure(cfg);
        db.setGroup(group);
        this.loadedObjects.add("group:" + cfg.getName());
    }
    
    private void linkGroup(GroupCfg cfg, BergamotDB db)
    {
        Group child = db.getGroup(cfg.getId());
        if (child != null)
        {
            for (String parentName : cfg.resolve().getGroups())
            {
                Group parent = db.getGroupByName(this.site.getId(), parentName);
                if (parent != null)
                {
                    this.report.info("Adding group " + child.getName() + " to group " + parent.getName());
                    db.addGroupChild(parent, child);
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
                if (ObjectState.isRemove(cfg.getObjectState()))
                {
                    this.removeTimePeriod(cfg, db);
                }
                else
                {
                    this.loadTimePeriod(cfg, db);
                }
            }
        }
        // link excludes
        for (TimePeriodCfg cfg : this.config.getTimePeriods())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                if (ObjectState.isChange(cfg.getObjectState()))
                {
                    this.linkTimePeriod(cfg, db);
                }
            }
        }
        // load any timeperiod where a template change cascades
        for (CascadedChange change : this.cascadedChanges.values())
        {
            if (change.dependent.getConfiguration() instanceof TimePeriodCfg)
            {
                TimePeriodCfg cfg = (TimePeriodCfg) change.dependent.getConfiguration();
                if (! (cfg.getTemplateBooleanValue() || this.loadedObjects.contains("timeperiod:" + cfg.getName())))
                {
                    this.report.info("Reconfiguring time period " + cfg.getName() + " due to a change to the " + change.template.getName() + " inherited template.");
                    // first we need to resolve the inheritance for the cascaded object
                    db.getConfigResolver(this.site.getId()).resolveInherit(cfg);
                    // load
                    this.loadTimePeriod(cfg, db);
                    this.linkTimePeriod(cfg, db);
                }
            }
        }
    }
    
    private void removeTimePeriod(TimePeriodCfg cfg, BergamotDB db)
    {
        this.report.info("Remove timeperiod: " + cfg.resolve().getName());
        TimePeriod timePeriod = db.getTimePeriodByName(this.site.getId(), cfg.getName());
        if (timePeriod != null)
        {
            db.removeTimePeriod(timePeriod.getId());
        }
    }
    
    private void loadTimePeriod(TimePeriodCfg cfg, BergamotDB db)
    {
        if (this.loadedObjects.contains("timeperiod:" + cfg.getName()))
        {
            this.report.info("Skipping reconfiguring timeperiod " + cfg.getName());
            return;
        }
        // load
        TimePeriod timePeriod = db.getTimePeriodByName(this.site.getId(), cfg.getName());
        if (timePeriod == null)
        {
            cfg.setId(this.site.randomObjectId());
            timePeriod = new TimePeriod();
            this.report.info("Configuring new timeperiod: " + cfg.resolve().getName());
        }
        else
        {
            cfg.setId(timePeriod.getId());
            this.report.info("Reconfiguring existing timeperiod: " + cfg.resolve().getName() + " (" + cfg.getId() + ")");
        }
        timePeriod.configure(cfg);
        db.setTimePeriod(timePeriod);
        this.loadedObjects.add("timeperiod:" + cfg.getName());
    }
    
    private void linkTimePeriod(TimePeriodCfg cfg, BergamotDB db)
    {
        TimePeriod timePeriod = db.getTimePeriod(cfg.getId());
        if (timePeriod != null)
        {
            for (String excludeName : cfg.resolve().getExcludes())
            {
                TimePeriod excluded = db.getTimePeriodByName(this.site.getId(), excludeName);
                if (excluded != null)
                {
                    this.report.info("Adding excluded timeperiod " + excluded.getName() + " to timeperiod " + timePeriod.getName());
                    db.addTimePeriodExclude(timePeriod, excluded);
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
                if (ObjectState.isRemove(cfg.getObjectState()))
                {
                    this.removeTeam(cfg, db);
                }
                else
                {
                    this.loadTeam(cfg, db);
                }
            }
        }
        // link the tree
        for (TeamCfg cfg : this.config.getTeams())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                if (ObjectState.isChange(cfg.getObjectState()))
                {
                    this.linkTeam(cfg, db);
                }
            }
        }
        // load any team where a template change cascades
        for (CascadedChange change : this.cascadedChanges.values())
        {
            if (change.dependent.getConfiguration() instanceof TeamCfg)
            {
                TeamCfg cfg = (TeamCfg) change.dependent.getConfiguration();
                if (! (cfg.getTemplateBooleanValue() || this.loadedObjects.contains("team:" + cfg.getName())))
                {
                    this.report.info("Reconfiguring team " + cfg.getName() + " due to a change to the " + change.template.getName() + " inherited template.");
                    // first we need to resolve the inheritance for the cascaded object
                    db.getConfigResolver(this.site.getId()).resolveInherit(cfg);
                    // load
                    this.loadTeam(cfg, db);
                    this.linkTeam(cfg, db);
                }
            }
        }
    }
    
    private void removeTeam(TeamCfg cfg, BergamotDB db)
    {
        this.report.info("Remove team: " + cfg.resolve().getName());
        Team team = db.getTeamByName(this.site.getId(), cfg.getName());
        if (team != null)
        {
            db.removeTeam(team.getId());
        }
    }
    
    private void loadTeam(TeamCfg cfg, BergamotDB db)
    {
        if (this.loadedObjects.contains("team:" + cfg.getName()))
        {
            this.report.info("Skipping reconfiguring team " + cfg.getName());
            return;
        }
        // load
        Team team = db.getTeamByName(this.site.getId(), cfg.getName());
        if (team == null)
        {
            cfg.setId(this.site.randomObjectId());
            team = new Team();
            this.report.info("Configuring new team: " + cfg.resolve().getName());
        }
        else
        {
            cfg.setId(team.getId());
            this.report.info("Reconfiguring existing team: " + cfg.resolve().getName() + " (" + cfg.getId() + ")");
        }
        team.configure(cfg);
        db.setTeam(team);
        this.loadedObjects.add("team:" + cfg.getName());
    }
    
    private void linkTeam(TeamCfg cfg, BergamotDB db)
    {
        Team child = db.getTeam(cfg.getId());
        if (child != null)
        {
            for (String parentName : cfg.resolve().getTeams())
            {
                Team parent = db.getTeamByName(this.site.getId(), parentName);
                if (parent != null)
                {
                    this.report.info("Adding team " + child.getName() + " to team " + parent.getName());
                    db.addTeamChild(parent, child);
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
                this.loadContact(cfg, db);
            }
        }
        // load any contact where a template change cascades
        for (CascadedChange change : this.cascadedChanges.values())
        {
            if (change.dependent.getConfiguration() instanceof ContactCfg)
            {
                ContactCfg cfg = (ContactCfg) change.dependent.getConfiguration();
                if (! (cfg.getTemplateBooleanValue() || this.loadedObjects.contains("team:" + cfg.getName())))
                {
                    this.report.info("Reconfiguring contact " + cfg.getName() + " due to a change to the " + change.template.getName() + " inherited template.");
                    // first we need to resolve the inheritance for the cascaded object
                    db.getConfigResolver(this.site.getId()).resolveInherit(cfg);
                    // load
                    this.loadContact(cfg, db);
                }
            }
        }
    }
    
    private void loadContact(ContactCfg cfg, BergamotDB db)
    {
        ContactCfg rcfg = cfg.resolve();
        // load
        Contact contact = db.getContactByName(this.site.getId(), rcfg.getName());
        if (contact == null)
        {
            cfg.setId(this.site.randomObjectId());
            contact = new Contact();
            // set a default password
            // TODO 
            contact.hashPassword("bergamot");
            contact.setForcePasswordChange(true);
            this.report.info("Configuring new contact: " + cfg.resolve().getName());
        }
        else
        {
            cfg.setId(contact.getId());
            this.report.info("Reconfiguring existing contact: " + cfg.resolve().getName() + " (" + cfg.getId() + ")");
        }
        contact.configure(cfg);
        // notifications
        this.loadNotifications(contact.getId(), rcfg.getNotifications(), db);
        // store
        db.setContact(contact);
        this.loadedObjects.add("contact:" + cfg.getName());
        // teams
        for (String teamName : rcfg.getTeams())
        {
            Team team = db.getTeamByName(this.site.getId(), teamName);
            if (team != null)
            {
                this.report.info("Adding contact " + contact.getName() + " to team " + team.getName());
                team.addContact(contact);
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
        // load all directly modified hosts
        for (HostCfg cfg : this.config.getHosts())
        {
            if (! cfg.getTemplateBooleanValue())
            {
                this.loadHost(cfg, db);
            }
        }
        // load any hosts where a template change cascades
        // Note: we don't need to separately handle service and trap 
        //       template changes as these are caught by reconfiguring 
        //       the host they exist on
        for (CascadedChange change : this.cascadedChanges.values())
        {
            if (change.dependent.getConfiguration() instanceof HostCfg)
            {
                HostCfg cfg = (HostCfg) change.dependent.getConfiguration();
                if (! (cfg.getTemplateBooleanValue() || this.loadedObjects.contains("host:" + cfg.getName())))
                {
                    this.report.info("Reconfiguring host " + cfg.getName() + " due to a change to the " + change.template.getName() + " inherited template.");
                    // first we need to resolve the inheritance for the cascaded object
                    db.getConfigResolver(this.site.getId()).resolveInherit(cfg);
                    // load
                    this.loadHost(cfg, db);
                }
            }
        }
    }
    
    private void loadHost(HostCfg cfg, BergamotDB db)
    {
        HostCfg rcfg = cfg.resolve();
        if (this.loadedObjects.contains("host:" + cfg.getName()))
        {
            this.report.info("Skipping reconfiguring host " + cfg.getName());
            return;
        }
        this.report.info("Loading host:\r\n" + rcfg.toString());
        // load
        DelayedSchedulerAction.SchedulingChange hostSchedulingChange = null;
        Host host = db.getHostByName(this.site.getId(), rcfg.getName());
        if (host == null)
        {
            cfg.setId(this.site.randomObjectId());
            host = new Host();
            hostSchedulingChange = DelayedSchedulerAction.SchedulingChange.SCHEDULE;
            this.report.info("Configuring new host: " + rcfg.getName());
        }
        else
        {
            cfg.setId(host.getId());
            hostSchedulingChange = DelayedSchedulerAction.SchedulingChange.RESCHEDULE;
            this.report.info("Reconfiguring existing host: " + rcfg.getName() + " (" + cfg.getId() + ")");
        }
        host.configure(cfg);
        // load the check details
        this.loadActiveCheck(host, rcfg, db);
        // add locations
        if (host.getLocationId() != null)
            db.invalidateHostsInLocation(host.getLocationId());
        host.setLocationId(null);
        String locationName = rcfg.getLocation();
        if (!Util.isEmpty(locationName))
        {
            Location location = db.getLocationByName(this.site.getId(), locationName);
            if (location != null)
            {
                host.setLocationId(location.getId());
                db.invalidateHostsInLocation(location.getId());
            }
        }
        // add the host
        db.setHost(host);
        this.delayedSchedulerActions.add(new DelayedSchedulerAction(hostSchedulingChange, host));
        this.loadedObjects.add("host:" + cfg.getName());
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
                this.report.info("Added command " + command.getName() + " to check " + check.getName());
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
        for (UUID oldGroupId : check.getGroupIds())
        {
            db.invalidateChecksInGroup(oldGroupId);
        }
        check.getGroupIds().clear();
        for (String groupName : rcfg.getGroups())
        {
            Group group = db.getGroupByName(this.site.getId(), groupName);
            if (group != null)
            {
                this.report.info("Adding check " + check.getName() + " to group " + group.getName());
                check.getGroupIds().add(group.getId());
                db.invalidateChecksInGroup(group.getId());
            }
        }
    }

    private void loadService(Host host, ServiceCfg cfg, BergamotDB db)
    {
        // resolve
        ServiceCfg rcfg = cfg.resolve();
        // create the service
        DelayedSchedulerAction.SchedulingChange serviceSchedulingChange = null;
        Service service = db.getServiceOnHost(host.getId(), rcfg.getName());
        if (service == null)
        {
            cfg.setId(this.site.randomObjectId());
            service = new Service();
            service.setHostId(host.getId());
            serviceSchedulingChange = DelayedSchedulerAction.SchedulingChange.SCHEDULE;
            this.report.info("Configuring new service: " + cfg.resolve().getName() + " on host " + host.getName());
        }
        else
        {
            cfg.setId(service.getId());
            serviceSchedulingChange = DelayedSchedulerAction.SchedulingChange.RESCHEDULE;
            this.report.info("Reconfiguring existing service: " + cfg.resolve().getName() + " on host " + host.getName() + " (" + cfg.getId() + ")");
        }
        service.configure(cfg);
        // load the check details
        this.loadActiveCheck(service, rcfg, db);
        // add
        db.setService(service);
        this.delayedSchedulerActions.add(new DelayedSchedulerAction(serviceSchedulingChange, service));
    }
    
    private void loadTrap(Host host, TrapCfg cfg, BergamotDB db)
    {
        // resolve
        TrapCfg rcfg = cfg.resolve();
        // create the service
        Trap trap = db.getTrapOnHost(host.getId(), cfg.getName());
        if (trap == null)
        {
            cfg.setId(this.site.randomObjectId());
            trap = new Trap();
            trap.setHostId(host.getId());
            this.report.info("Configuring new trap: " + cfg.resolve().getName() + " on host " + host.getName());
        }
        else
        {
            cfg.setId(trap.getId());
            this.report.info("Reconfiguring existing trap: " + cfg.resolve().getName() + " on host " + host.getName() + " (" + cfg.getId() + ")");
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
                this.report.info("Using virtual check condition " + cond.toString() + " for " + check);
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
                
            }
        }
        // load any clusters where a template change cascades
        // Note: we don't need to separately handle resources 
        //       template changes as these are caught by reconfiguring 
        //       the cluster they exist on
        for (CascadedChange change : this.cascadedChanges.values())
        {
            if (change.dependent.getConfiguration() instanceof ClusterCfg)
            {
                ClusterCfg cfg = (ClusterCfg) change.dependent.getConfiguration();
                if (! (cfg.getTemplateBooleanValue() || this.loadedObjects.contains("cluster:" + cfg.getName())))
                {
                    this.report.info("Reconfiguring cluster " + cfg.getName() + " due to a change to the " + change.template.getName() + " inherited template.");
                    // first we need to resolve the inheritance for the cascaded object
                    db.getConfigResolver(this.site.getId()).resolveInherit(cfg);
                    // load
                    this.loadCluster(cfg, db);
                }
            }
        }
    }
    
    private void loadCluster(ClusterCfg cfg, BergamotDB db)
    {
        if (this.loadedObjects.contains("cluster:" + cfg.getName()))
        {
            this.report.info("Skipping reconfiguring cluster " + cfg.getName());
            return;
        }
        // resolved config
        ClusterCfg rcfg = cfg.resolve();
        // load
        Cluster cluster = db.getClusterByName(this.site.getId(), cfg.getName());
        if (cluster == null)
        {
            cfg.setId(this.site.randomObjectId());
            cluster = new Cluster();
            this.report.info("Configuring new cluster: " + cfg.resolve().getName());
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
        this.loadedObjects.add("cluster:" + cfg.getName());
        // add resources
        for (ResourceCfg scfg : rcfg.getResources())
        {
            this.loadResource(cluster, scfg, db);
        }
    }
    
    private void loadResource(Cluster cluster, ResourceCfg cfg, BergamotDB db)
    {
        // resolve
        ResourceCfg rcfg = cfg.resolve();
        // create the service
        Resource resource = db.getResourceOnCluster(cluster.getId(), rcfg.getName());
        if (resource == null)
        {
            cfg.setId(this.site.randomObjectId());
            resource = new Resource();
            resource.setClusterId(cluster.getId());
            this.report.info("Configuring new resource: " + cfg.resolve().getName() + " on cluster " + cluster.getName());
        }
        else
        {
            cfg.setId(resource.getId());
            this.report.info("Reconfiguring existing group: " + cfg.resolve().getName() + " on cluster " + cluster.getName() + " (" + cfg.getId() + ")");
        }
        resource.configure(cfg);
        // load the check details
        this.loadVirtualCheck(resource, rcfg, db);
        // add
        db.setResource(resource);
    }
    
    private static class CascadedChange
    {
        public final Config dependent;
        
        public final Config template;
        
        public final TemplatedObjectCfg<?> change;
        
        public CascadedChange(TemplatedObjectCfg<?> change, Config template, Config dependent)
        {
            this.change = change;
            this.template = template;
            this.dependent = dependent;
        }
    }
    
    private static class DelayedSchedulerAction
    {
        public enum SchedulingChange { SCHEDULE, RESCHEDULE, REMOVE }
        
        public final ActiveCheck<?,?> check;
        
        public final SchedulingChange change;
        
        public DelayedSchedulerAction(SchedulingChange change, ActiveCheck<?,?> check)
        {
            this.change = change;
            this.check = check;
        }
    }
}
