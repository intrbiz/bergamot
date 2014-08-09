package com.intrbiz.bergamot.data;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.CheckCommand;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Comment;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Downtime;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.NotificationEngine;
import com.intrbiz.bergamot.model.Notifications;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.VirtualCheck;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.state.GroupState;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;
import com.intrbiz.data.DataManager;
import com.intrbiz.data.cache.Cache;
import com.intrbiz.data.cache.CacheInvalidate;
import com.intrbiz.data.cache.Cacheable;
import com.intrbiz.data.db.DatabaseAdapter;
import com.intrbiz.data.db.DatabaseConnection;
import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.meta.Direction;
import com.intrbiz.data.db.compiler.meta.SQLGetter;
import com.intrbiz.data.db.compiler.meta.SQLLimit;
import com.intrbiz.data.db.compiler.meta.SQLOffset;
import com.intrbiz.data.db.compiler.meta.SQLOrder;
import com.intrbiz.data.db.compiler.meta.SQLParam;
import com.intrbiz.data.db.compiler.meta.SQLQuery;
import com.intrbiz.data.db.compiler.meta.SQLRemove;
import com.intrbiz.data.db.compiler.meta.SQLSchema;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.meta.SQLVersion;

@SQLSchema(
        name = "bergamot", 
        version = @SQLVersion({1, 1, 1}),
        tables = {
            Site.class,
            Location.class,
            Group.class,
            TimePeriod.class,
            Command.class,
            Team.class,
            Contact.class,
            Notifications.class,
            NotificationEngine.class,
            CheckState.class,
            CheckCommand.class,
            Host.class,
            Service.class,
            Trap.class,
            Cluster.class,
            Resource.class,
            GroupState.class,
            Alert.class,
            Config.class,
            Comment.class,
            Downtime.class
        }
)
public abstract class BergamotDB extends DatabaseAdapter
{
    /**
     * Compile and register the Bergamot Database Adapter
     */
    static
    {
        DataManager.getInstance().registerDatabaseAdapter(
                BergamotDB.class, 
                DatabaseAdapterCompiler.defaultPGSQLCompiler().compileAdapterFactory(BergamotDB.class)
        );
    }
    
    /**
     * Install the Bergamot schema into the default database
     */
    public static void install()
    {
        Logger logger = Logger.getLogger(BergamotDB.class);
        DatabaseConnection database = DataManager.getInstance().connect();
        DatabaseAdapterCompiler compiler =  DatabaseAdapterCompiler.defaultPGSQLCompiler().setDefaultOwner("bergamot");
        // check if the schema is installed
        if (! compiler.isSchemaInstalled(database, BergamotDB.class))
        {
            logger.info("Installing database schema");
            compiler.installSchema(database, BergamotDB.class);
        }
        else
        {
            // check the installed schema is upto date
            if (! compiler.isSchemaUptoDate(database, BergamotDB.class))
            {
                logger.info("The installed database schema is not upto date");
                compiler.upgradeSchema(database, BergamotDB.class);
            }
            else
            {
                logger.info("The installed database schema is upto date");
            }
        }
    }

    /**
     * Connect to the Bergamot database
     */
    public static BergamotDB connect()
    {
        return DataManager.getInstance().databaseAdapter(BergamotDB.class);
    }
    
    /**
     * Connect to the Bergamot database
     */
    public static BergamotDB connect(DatabaseConnection connection)
    {
        return DataManager.getInstance().databaseAdapter(BergamotDB.class, connection);
    }

    public BergamotDB(DatabaseConnection connection, Cache cache)
    {
        super(connection, cache);
    }
    
    // the schema
    
    // site
    
    @Cacheable
    @CacheInvalidate({"get_site_by_name.*"})
    @SQLSetter(table = Site.class, name = "set_site", since = @SQLVersion({1, 0, 0}))
    public abstract void setSite(Site site);
    
    @Cacheable
    @SQLGetter(table = Site.class, name = "get_site", since = @SQLVersion({1, 0, 0}))
    public abstract Site getSite(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Site.class, name ="get_site_by_name", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.site WHERE name = p_name OR aliases @> ARRAY[p_name]")
    )
    public abstract Site getSiteByName(@SQLParam("name") String name);
    
    @Cacheable
    @CacheInvalidate({"get_site_by_name.*"})
    @SQLRemove(table = Site.class, name = "remove_site", since = @SQLVersion({1, 0, 0}))
    public abstract void removeSite(@SQLParam("id") UUID id);
    
    @SQLGetter(table = Site.class, name = "list_sites", since = @SQLVersion({1, 0, 0}))
    public abstract List<Site> listSites();
    
    // config
    
    @SQLSetter(table = Config.class, name = "set_config", since = @SQLVersion({1, 0, 0}))
    public abstract void setConfig(Config template);
    
    @SQLGetter(table = Config.class, name = "get_config", since = @SQLVersion({1, 0, 0}))
    public abstract Config getConfig(@SQLParam("id") UUID id);
    
    @SQLRemove(table = Config.class, name = "remove_config", since = @SQLVersion({1, 0, 0}))
    public abstract void removeConfig(@SQLParam("id") UUID id);
    
    @SQLGetter(table = Config.class, name = "list_config", since = @SQLVersion({1, 0, 0}))
    public abstract List<Config> listConfig(@SQLParam("site_id") UUID siteId, @SQLParam(value = "type", optional = true) String type);
    
    @SQLGetter(table = Config.class, name = "list_config_templates", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.config WHERE site_id = p_site_id AND type = p_type AND template = TRUE")
    )
    public abstract List<Config> listConfigTemplates(@SQLParam("site_id") UUID siteId, @SQLParam(value = "type") String type);
    
    @SQLGetter(table = Config.class, name = "get_config_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Config getConfigByName(@SQLParam("site_id") UUID siteId, @SQLParam("type") String type, @SQLParam("name") String name);
    
    public ConfigResolver getConfigResolver(UUID siteId)
    {
        return new ConfigResolver(this, siteId);
    }
    
    // time period
    
    @Cacheable
    @CacheInvalidate({"get_timeperiod_by_name.#{site_id}.*"})
    @SQLSetter(table = TimePeriod.class, name = "set_timeperiod", since = @SQLVersion({1, 0, 0}))
    public abstract void setTimePeriod(TimePeriod timePeriod);
    
    @Cacheable
    @SQLGetter(table = TimePeriod.class, name = "get_timeperiod", since = @SQLVersion({1, 0, 0}))
    public abstract TimePeriod getTimePeriod(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = TimePeriod.class, name = "get_timeperiod_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract TimePeriod getTimePeriodByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @SQLGetter(table = TimePeriod.class, name = "list_timeperiods", since = @SQLVersion({1, 0, 0}))
    public abstract List<TimePeriod> listTimePeriods(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @CacheInvalidate({"get_timeperiod_by_name.#{this.getSiteId(id)}.*"})
    @SQLRemove(table = TimePeriod.class, name = "remove_timeperiod", since = @SQLVersion({1, 0, 0}))
    public abstract void removeTimePeriod(@SQLParam("id") UUID id);
    
    public void addTimePeriodExclude(TimePeriod timePeriod, TimePeriod excluded)
    {
        if (! timePeriod.getExcludesId().contains(excluded.getId()))
        {
            timePeriod.getExcludesId().add(excluded.getId());
        }
        this.setTimePeriod(timePeriod);
    }
    
    public void removeTimePeriodExclude(TimePeriod timePeriod, TimePeriod excluded)
    {
        timePeriod.getExcludesId().remove(excluded.getId());
        this.setTimePeriod(timePeriod);
    }
    
    // command
    
    @Cacheable
    @CacheInvalidate({"get_command_by_name.#{site_id}.*"})
    @SQLSetter(table = Command.class, name = "set_command", since = @SQLVersion({1, 0, 0}))
    public abstract void setCommand(Command command);
    
    @Cacheable
    @SQLGetter(table = Command.class, name = "get_command", since = @SQLVersion({1, 0, 0}))
    public abstract Command getCommand(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Command.class, name = "get_command_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Command getCommandByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @SQLGetter(table = Command.class, name = "list_commands", since = @SQLVersion({1, 0, 0}))
    public abstract List<Command> listCommands(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @CacheInvalidate({"get_command_by_name.#{this.getSiteId(id)}.*"})
    @SQLRemove(table = Command.class, name = "remove_command", since = @SQLVersion({1, 0, 0}))
    public abstract void removeCommand(@SQLParam("id") UUID id);
    
    // location
    
    @Cacheable
    @CacheInvalidate({"get_root_locations.#{site_id}", "get_location_by_name.#{site_id}.*", "get_locations_in_location.#{id}"})
    @SQLSetter(table = Location.class, name = "set_location", since = @SQLVersion({1, 0, 0}))
    public abstract void setLocation(Location location);
    
    @Cacheable
    @SQLGetter(table = Location.class, name = "get_location", since = @SQLVersion({1, 0, 0}))
    public abstract Location getLocation(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Location.class, name = "get_location_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Location getLocationByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Location.class, name = "get_locations_in_location", since = @SQLVersion({1, 0, 0}))
    public abstract List<Location> getLocationsInLocation(@SQLParam("location_id") UUID locationId);
    
    @SQLGetter(table = Location.class, name = "list_locations", since = @SQLVersion({1, 0, 0}))
    public abstract List<Location> listLocations(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @SQLGetter(table = Location.class, name = "get_root_locations", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.location WHERE site_id = p_site_id AND location_id IS NULL")
    )
    public abstract List<Location> getRootLocations(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @CacheInvalidate({"get_root_locations.#{this.getSiteId(id)}", "get_location_by_name.#{this.getSiteId(id)}.*", "get_locations_in_location.#{id}"})
    @SQLRemove(table = Location.class, name = "remove_location", since = @SQLVersion({1, 0, 0}))
    public abstract void removeLocation(@SQLParam("id") UUID locationId);
    
    
    public void addLocationChild(Location parent, Location child)
    {
        child.setLocationId(parent.getId());
        this.setLocation(child);
        this.getAdapterCache().remove("get_locations_in_location." + parent.getId());
    }
    
    public void removeLocationChild(Location parent, Location child)
    {
        child.setLocationId(null);
        this.setLocation(child);
        this.getAdapterCache().remove("get_locations_in_location." + parent.getId());
    }
    
    public void addLocationHost(Location location, Host host)
    {
        host.setLocationId(location.getId());
        this.setHost(host);
        this.getAdapterCache().remove("get_hosts_in_location." + location.getId());
    }
    
    public void removeLocationHost(Location location, Host host)
    {
        host.setLocationId(null);
        this.setHost(host);
        this.getAdapterCache().remove("get_hosts_in_location." + location.getId());
    }
    
    // group
    
    @Cacheable
    @CacheInvalidate({"get_group_by_name.#{site_id}.*", "get_root_groups.#{site_id}.*", "get_groups_in_group.#{id}"})
    @SQLSetter(table = Group.class, name = "set_group", since = @SQLVersion({1, 0, 0}))
    public abstract void setGroup(Group group);
    
    @Cacheable
    @SQLGetter(table = Group.class, name = "get_group", since = @SQLVersion({1, 0, 0}))
    public abstract Group getGroup(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Group.class, name = "get_group_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Group getGroupByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @SQLGetter(table = Group.class, name = "list_group", since = @SQLVersion({1, 0, 0}))
    public abstract List<Group> listGroups(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @SQLGetter(table = Group.class, name = "get_root_groups", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.group WHERE site_id = p_site_id AND (group_ids IS NULL OR group_ids = ARRAY[]::UUID[])")
    )
    public abstract List<Group> getRootGroups(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @SQLGetter(table = Group.class, name = "get_groups_in_group", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.group WHERE group_ids @> ARRAY[p_group_id]")
    )
    public abstract List<Group> getGroupsInGroup(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    @Cacheable
    @CacheInvalidate({"get_group_by_name.#{this.getSiteId(id)}.*", "get_root_groups.#{this.getSiteId(id)}.*", "get_groups_in_group.#{id}"})
    @SQLRemove(table = Group.class, name = "remove_group", since = @SQLVersion({1, 0, 0}))
    public abstract void removeGroup(@SQLParam("id") UUID id);
    
    public void addGroupChild(Group parent, Group child)
    {
        if (! child.getGroupIds().contains(parent.getId()))
        {
            child.getGroupIds().add(parent.getId());
        }   
        this.setGroup(child);
        this.getAdapterCache().remove("get_groups_in_group." + parent.getId());
    }
    
    public void removeGroupChild(Group parent, Group child)
    {
        child.getGroupIds().remove(parent.getId());
        this.setGroup(child);
        this.getAdapterCache().remove("get_groups_in_group." + parent.getId());
    }
    
    public void addCheckToGroup(Group group, Check<?,?> check)
    {
        if (check.getGroupIds().contains(group.getId()))
        {
            check.getGroupIds().add(group.getId());
        }
        this.setCheck(check);
        this.getAdapterCache().remove("get_hosts_in_group." + group.getId());
        this.getAdapterCache().remove("get_services_in_group." + group.getId());
        this.getAdapterCache().remove("get_traps_in_group." + group.getId());
        this.getAdapterCache().remove("get_clusters_in_group." + group.getId());
        this.getAdapterCache().remove("get_resources_in_group." + group.getId());
    }
    
    public void removeCheckFromGroup(Group group, Check<?,?> check)
    {
        check.getGroupIds().remove(group.getId());
        this.setCheck(check);
        this.getAdapterCache().remove("get_hosts_in_group." + group.getId());
        this.getAdapterCache().remove("get_services_in_group." + group.getId());
        this.getAdapterCache().remove("get_traps_in_group." + group.getId());
        this.getAdapterCache().remove("get_clusters_in_group." + group.getId());
        this.getAdapterCache().remove("get_resources_in_group." + group.getId());
    }
    
    // team
    
    @Cacheable
    @CacheInvalidate({"get_team_by_name.#{site_id}.*", "get_teams_in_team.#{id}", "get_contacts_in_team.#{id}"})
    @SQLSetter(table = Team.class, name = "set_team", since = @SQLVersion({1, 0, 0}))
    public abstract void setTeam(Team team);
    
    @Cacheable
    @SQLGetter(table = Team.class, name = "get_team", since = @SQLVersion({1, 0, 0}))
    public abstract Team getTeam(@SQLParam("id") UUID id);
    
    @SQLGetter(table = Team.class, name = "list_teams", since = @SQLVersion({1, 0, 0}))
    public abstract List<Team> listTeams(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @SQLGetter(table = Team.class, name = "get_team_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Team getTeamByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Team.class, name = "get_teams_in_team", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.team WHERE team_ids @> ARRAY[p_team_id]")
    )
    public abstract List<Team> getTeamsInTeam(@SQLParam(value = "team_id", virtual = true) UUID teamId);
    
    @Cacheable
    @CacheInvalidate({"get_team_by_name.#{this.getSiteId(id)}.*", "get_teams_in_team.#{id}", "get_contacts_in_team.#{id}"})
    @SQLRemove(table = Team.class, name = "remove_team", since = @SQLVersion({1, 0, 0}))
    public abstract void removeTeam(@SQLParam("id") UUID id);
    
    public void addTeamChild(Team parent, Team child)
    {
        if (! child.getTeamIds().contains(parent))
        {
            child.getTeamIds().add(parent.getId());
        }
        this.setTeam(child);
        this.getAdapterCache().remove("get_teams_in_team." + parent.getId());
    }
    
    public void removeTeamChild(Team parent, Team child)
    {
        child.getTeamIds().remove(parent.getId());
        this.setTeam(child);
        this.getAdapterCache().remove("get_teams_in_team." + parent.getId());
    }
    
    public void addContactToTeam(Team parent, Contact contact)
    {
        if (! contact.getTeamIds().contains(parent.getId()))
        {
            contact.getTeamIds().add(parent.getId());
        }
        this.setContact(contact);
        this.getAdapterCache().remove("get_contacts_in_team." + parent.getId());
    }
    
    public void removeContactFromTeam(Team parent, Contact contact)
    {
        contact.getTeamIds().remove(parent.getId());
        this.setContact(contact);
        this.getAdapterCache().remove("get_contacts_in_team." + parent.getId());
    }
    
    // contact
    
    @Cacheable
    @CacheInvalidate({"get_contact_by_name.#{site_id}.*", "get_contact_by_email.#{site_id}.*", "get_contact_by_name_or_email.#{site_id}.*"})
    @SQLSetter(table = Contact.class, name = "set_contact", since = @SQLVersion({1, 0, 0}))
    public abstract void setContact(Contact contact);
    
    @Cacheable
    @SQLGetter(table = Contact.class, name = "get_contact", since = @SQLVersion({1, 0, 0}))
    public abstract Contact getContact(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Contact.class, name = "get_contact_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Contact getContactByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Contact.class, name = "get_contact_by_email", since = @SQLVersion({1, 0, 0}))
    public abstract Contact getContactByEmail(@SQLParam("site_id") UUID siteId, @SQLParam("email") String email);
    
    @Cacheable
    @SQLGetter(table = Contact.class, name = "get_contact_by_name_or_email", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.contact WHERE name = p_name_or_email OR email = p_name_or_email")
    )
    public abstract Contact getContactByNameOrEmail(@SQLParam("site_id") UUID siteId, @SQLParam(value = "name_or_email", virtual = true) String nameOrEmail);
    
    @SQLGetter(table = Contact.class, name = "list_contacts", since = @SQLVersion({1, 0, 0}))
    public abstract List<Contact> listContacts(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @SQLGetter(table = Contact.class, name = "get_contacts_in_team", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.contact WHERE team_ids @> ARRAY[p_team_id]")
    )
    public abstract List<Contact> getContactsInTeam(@SQLParam(value = "team_id", virtual = true) UUID teamId);
    
    @SQLGetter(table = Contact.class, name = "get_contacts_not_in_a_team", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM contact WHERE site_id = p_site_id AND (team_ids IS NULL OR team_ids = ARRAY[]::UUID[])")
    )
    public abstract List<Contact> getContactsNotInATeam(@SQLParam("site_id") UUID site_id);
    
    @Cacheable
    @CacheInvalidate({"get_contact_by_name.#{this.getSiteId(id)}.*", "get_contact_by_email.#{this.getSiteId(id)}.*", "get_contact_by_name_or_email.#{this.getSiteId(id)}.*"})
    @SQLRemove(table = Contact.class, name = "remove_contact", since = @SQLVersion({1, 0, 0}))
    public abstract void removeContact(@SQLParam("id") UUID id);
    
    // notifications
    
    @Cacheable
    @CacheInvalidate({"get_notification_engines.#{id}"})
    @SQLSetter(table = Notifications.class, name = "set_notifications", since = @SQLVersion({1, 0, 0}))
    public abstract void setNotifications(Notifications notifications);
    
    @Cacheable
    @SQLGetter(table = Notifications.class, name = "get_notifications", since = @SQLVersion({1, 0, 0}))
    public abstract Notifications getNotifications(@SQLParam("id") UUID id);
    
    @Cacheable
    @CacheInvalidate({"get_notification_engines.#{id}"})
    @SQLRemove(table = Notifications.class, name = "remove_notifications", since = @SQLVersion({1, 0, 0}))
    public abstract void removeNotifications(@SQLParam("id") UUID id);
    
    // notification engine
    
    @Cacheable
    @CacheInvalidate({"get_notification_engines.#{notifications_id}"})
    @SQLSetter(table = NotificationEngine.class, name = "set_notification_engine", since = @SQLVersion({1, 0, 0}))
    public abstract void setNotificationEngine(NotificationEngine notificationEngine);
    
    @Cacheable
    @SQLGetter(table = NotificationEngine.class, name = "get_notification_engine", since = @SQLVersion({1, 0, 0}))
    public abstract NotificationEngine getNotificationEngine(@SQLParam("notifications_id") UUID notificationId, @SQLParam("engine") String engine);
    
    @Cacheable
    @SQLGetter(table = NotificationEngine.class, name = "get_notification_engines", since = @SQLVersion({1, 0, 0}))
    public abstract List<NotificationEngine> getNotificationEngines(@SQLParam("notifications_id") UUID notificationId);
    
    @Cacheable
    @CacheInvalidate({"get_notification_engines.#{notifications_id}"})
    @SQLRemove(table = NotificationEngine.class, name = "remove_notification_engine", since = @SQLVersion({1, 0, 0}))
    public abstract void removeNotificationEngine(@SQLParam("notifications_id") UUID notificationId, @SQLParam("engine") String engine);
    
    // state
    
    @Cacheable
    @SQLSetter(table = CheckState.class, name = "set_check_state", since = @SQLVersion({1, 0, 0}))
    public abstract void setCheckState(CheckState state);
    
    @Cacheable
    @SQLGetter(table = CheckState.class, name = "get_check_state", since = @SQLVersion({1, 0, 0}))
    public abstract CheckState getCheckState(@SQLParam("check_id") UUID id);
    
    @Cacheable
    @SQLRemove(table = CheckState.class, name = "remove_check_state", since = @SQLVersion({1, 0, 0}))
    public abstract void removeCheckState(@SQLParam("check_id") UUID id);
    
    // alerts
    
    @Cacheable
    @CacheInvalidate({"get_all_alerts_for_check.#{check_id}", "get_recovered_alerts_for_check.#{check_id}", "get_alerts_for_check.#{check_id}", "get_current_alert_for_check.#{check_id}"})
    @SQLSetter(table = Alert.class, name = "set_alert", since = @SQLVersion({1, 0, 0}))
    public abstract void setAlert(Alert alert);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_alert", since = @SQLVersion({1, 0, 0}))
    public abstract Alert getAlert(@SQLParam("id") UUID id);
    
    @Cacheable
    @CacheInvalidate({"get_all_alerts_for_check.*", "get_recovered_alerts_for_check.*", "get_alerts_for_check.*", "get_current_alert_for_check.*"})
    @SQLRemove(table = Alert.class, name = "remove_alert", since = @SQLVersion({1, 0, 0}))
    public abstract void removeAlert(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_all_alerts_for_check", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder(value = "raised", direction = Direction.DESC))
    public abstract List<Alert> getAllAlertsForCheck(@SQLParam("check_id") UUID checkId);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_recovered_alerts_for_check", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder(value = "raised", direction = Direction.DESC),
            query = @SQLQuery("SELECT * FROM bergamot.alert WHERE check_id = p_check_id AND recovered = TRUE")
    )
    public abstract List<Alert> getRecoveredAlertsForCheck(@SQLParam("check_id") UUID checkId);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_alerts_for_check", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder(value = "raised", direction = Direction.DESC),
            query = @SQLQuery("SELECT * FROM bergamot.alert WHERE check_id = p_check_id AND recovered = FALSE")
    )
    public abstract List<Alert> getAlertsForCheck(@SQLParam("check_id") UUID checkId);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_current_alert_for_check", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.alert WHERE check_id = p_check_id AND recovered = FALSE ORDER BY raised DESC LIMIT 1")
    )
    public abstract Alert getCurrentAlertForCheck(@SQLParam("check_id") UUID checkId);
    
    @SQLGetter(table = Alert.class, name = "list_alerts", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.alert WHERE site_id = p_site_id AND recovered = FALSE ORDER BY raised DESC")
    )
    public abstract List<Alert> listAlerts(@SQLParam("site_id") UUID siteId);
    
    // group state
    
    /**
     * Compute the state of the given group, this will recursively follow 
     * the group hierarchy an compute the state of all the checks within the 
     * group
     * @param groupId
     * @return
     */
    @SQLGetter(table = GroupState.class, name = "compute_group_state", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("WITH RECURSIVE group_graph(id) AS ( " +
                              "    SELECT g.id " +
                              "    FROM bergamot.group g " +
                              "    WHERE g.id = p_group_id " +
                              "  UNION " +
                              "    SELECT g.id " +
                              "    FROM bergamot.group g, group_graph gg " +
                              "    WHERE g.group_ids @> ARRAY[gg.id] " +
                              ") " +
                              "SELECT " + 
                              "  p_group_id, " +
                              "  bool_and(s.ok OR q.suppressed) AS ok, " + 
                              "  max(CASE WHEN q.suppressed THEN 0 ELSE s.status END)::INTEGER AS status, "+
                              "  count(CASE WHEN s.status = 0 AND NOT q.suppressed THEN 1 ELSE NULL END)::INTEGER AS pending_count, "+ 
                              "  count(CASE WHEN s.status = 1 AND NOT q.suppressed THEN 1 ELSE NULL END)::INTEGER AS ok_count, "+
                              "  count(CASE WHEN s.status = 2 AND NOT q.suppressed THEN 1 ELSE NULL END)::INTEGER AS warning_count, "+
                              "  count(CASE WHEN s.status = 3 AND NOT q.suppressed THEN 1 ELSE NULL END)::INTEGER AS critical_count, "+
                              "  count(CASE WHEN s.status = 4 AND NOT q.suppressed THEN 1 ELSE NULL END)::INTEGER AS unknown_count, "+
                              "  count(CASE WHEN s.status = 5 AND NOT q.suppressed THEN 1 ELSE NULL END)::INTEGER AS timeout_count, "+
                              "  count(CASE WHEN s.status = 6 AND NOT q.suppressed THEN 1 ELSE NULL END)::INTEGER AS error_count, "+
                              "  count(CASE WHEN q.suppressed                      THEN 1 ELSE NULL END)::INTEGER AS suppressed_count "+
                              "FROM bergamot.check_state s " +
                              "JOIN ( " +
                              "    SELECT id, suppressed, group_ids FROM bergamot.host " +
                              "  UNION " + 
                              "    SELECT id, suppressed, group_ids FROM bergamot.service " +
                              "  UNION  " +
                              "    SELECT id, suppressed, group_ids FROM bergamot.trap " +
                              "  UNION " +
                              "    SELECT id, suppressed, group_ids FROM bergamot.cluster " +
                              "  UNION " +
                              "    SELECT id, suppressed, group_ids FROM bergamot.resource " +
                              ") q " +
                              "ON (s.check_id = q.id) " +
                              "JOIN group_graph g " +
                              "ON (q.group_ids @> ARRAY[g.id])")
    )
    public abstract GroupState computeGroupState(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    /**
     * Compute the state of the given group, this will recursively follow 
     * the group hierarchy an compute the state of all the checks within the 
     * group
     * @param groupId
     * @return
     */
    @SQLGetter(table = GroupState.class, name = "compute_location_state", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("WITH RECURSIVE location_graph(id) AS ( "+
                              "    SELECT l.id "+
                              "    FROM bergamot.location l "+
                              "    WHERE l.id = p_location_id "+
                              "  UNION "+
                              "    SELECT l.id "+
                              "    FROM bergamot.location l, location_graph lg "+
                              "    WHERE l.location_id = lg.id "+
                              ") "+
                              "SELECT "+ 
                              "  p_location_id, "+
                              "  bool_and(s.ok OR h.suppressed) AS ok, "+ 
                              "  max(CASE WHEN h.suppressed THEN 0 ELSE s.status END)::INTEGER AS status, "+
                              "  count(CASE WHEN s.status = 0 AND NOT h.suppressed THEN 1 ELSE NULL END)::INTEGER AS pending_count, "+ 
                              "  count(CASE WHEN s.status = 1 AND NOT h.suppressed THEN 1 ELSE NULL END)::INTEGER AS ok_count, "+
                              "  count(CASE WHEN s.status = 2 AND NOT h.suppressed THEN 1 ELSE NULL END)::INTEGER AS warning_count, "+
                              "  count(CASE WHEN s.status = 3 AND NOT h.suppressed THEN 1 ELSE NULL END)::INTEGER AS critical_count, "+
                              "  count(CASE WHEN s.status = 4 AND NOT h.suppressed THEN 1 ELSE NULL END)::INTEGER AS unknown_count, "+
                              "  count(CASE WHEN s.status = 5 AND NOT h.suppressed THEN 1 ELSE NULL END)::INTEGER AS timeout_count, "+
                              "  count(CASE WHEN s.status = 6 AND NOT h.suppressed THEN 1 ELSE NULL END)::INTEGER AS error_count, "+
                              "  count(CASE WHEN h.suppressed                      THEN 1 ELSE NULL END)::INTEGER AS suppressed_count "+
                              "FROM bergamot.check_state s "+
                              "JOIN bergamot.host h "+
                              "ON (s.check_id = h.id) "+
                              "JOIN location_graph lg "+
                              "ON (h.location_id = lg.id)")
    )
    public abstract GroupState computeLocationState(@SQLParam(value = "location_id", virtual = true) UUID locationId);
    
    // check command
    
    @Cacheable
    @SQLSetter(table = CheckCommand.class, name = "set_check_command", since = @SQLVersion({1, 0, 0}))
    public abstract void setCheckCommand(CheckCommand command);
    
    @Cacheable
    @SQLGetter(table = CheckCommand.class, name = "get_check_command", since = @SQLVersion({1, 0, 0}))
    public abstract CheckCommand getCheckCommand(@SQLParam("check_id") UUID id);
    
    @Cacheable
    @SQLRemove(table = CheckCommand.class, name = "remove_check_command", since = @SQLVersion({1, 0, 0}))
    public abstract void removeCheckCommand(@SQLParam("check_id") UUID id);
    
    // host
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}", "get_host_by_name.#{site_id}.*", "get_host_by_address.#{site_id}.*"})
    @SQLSetter(table = Host.class, name = "set_host", since = @SQLVersion({1, 0, 0}))
    public abstract void setHost(Host host);
    
    @Cacheable
    @SQLGetter(table = Host.class, name = "get_host", since = @SQLVersion({1, 0, 0}))
    public abstract Host getHost(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Host.class, name = "get_host_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Host getHostByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Host.class, name = "get_host_by_address", since = @SQLVersion({1, 0, 0}))
    public abstract Host getHostByAddress(@SQLParam("site_id") UUID siteId, @SQLParam("address") String address);
    
    @Cacheable
    @SQLGetter(table = Host.class, name = "get_hosts_in_location", since = @SQLVersion({1, 0, 0}))
    public abstract List<Host> getHostsInLocation(@SQLParam("location_id") UUID locationId);
    
    @Cacheable
    @SQLGetter(table = Host.class, name = "get_hosts_in_group", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.host WHERE group_ids @> ARRAY[p_group_id]")
    )
    public abstract List<Host> getHostsInGroup(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    @SQLGetter(table = Host.class, name = "list_hosts", since = @SQLVersion({1, 0, 0}))
    public abstract List<Host> listHosts(@SQLParam("site_id") UUID siteId);
    
    @SQLGetter(table = Host.class, name = "list_hosts_that_are_not_ok", since = @SQLVersion({1, 0, 0}),
        query = @SQLQuery("SELECT c.* FROM bergamot.host c JOIN bergamot.check_state s ON (c.id = s.check_id) WHERE c.site_id = p_site_id AND (NOT s.ok) AND s.hard AND (NOT c.suppressed)")
    )
    public abstract List<Host> listHostsThatAreNotOk(@SQLParam("site_id") UUID siteId);
    
    @SQLGetter(table = Host.class, name = "list_hosts_in_pool", since = @SQLVersion({1, 0, 0}))
    public abstract List<Host> listHostsInPool(@SQLParam("site_id") UUID siteId, @SQLParam("pool") int pool);
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}", "get_host_by_name.#{this.getSiteId(id)}.*", "get_host_by_address.#{this.getSiteId(id)}.*"})
    @SQLRemove(table = Host.class, name = "remove_host", since = @SQLVersion({1, 0, 0}))
    public abstract void removeHost(@SQLParam("id") UUID id);
    
    public void addServiceToHost(Host host, Service service)
    {
        service.setHostId(host.getId());
        this.setService(service);
        this.getAdapterCache().remove("get_services_on_host." + host.getId());
    }
    
    public void removeServiceFromHost(Host host, Service service)
    {
        service.setHostId(null);
        this.setService(service);
        this.getAdapterCache().remove("get_services_on_host." + host.getId());
    }
    
    public void addTrapToHost(Host host, Trap trap)
    {
        trap.setHostId(host.getId());
        this.setTrap(trap);
        this.getAdapterCache().remove("get_traps_on_host." + host.getId());
    }
    
    public void removeTrapFromHost(Host host, Trap trap)
    {
        trap.setHostId(null);
        this.setTrap(trap);
        this.getAdapterCache().remove("get_traps_on_host." + host.getId());
    }
    
    // service
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}"})
    @SQLSetter(table = Service.class, name = "set_service", since = @SQLVersion({1, 0, 0}))
    public abstract void setService(Service service);
    
    @Cacheable
    @SQLGetter(table = Service.class, name = "get_service", since = @SQLVersion({1, 0, 0}))
    public abstract Service getService(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Service.class, name = "get_services_on_host", since = @SQLVersion({1, 0, 0}))
    public abstract List<Service> getServicesOnHost(@SQLParam("host_id") UUID hostId);
    
    @SQLGetter(table = Service.class, name = "get_service_on_host", since = @SQLVersion({1, 0, 0}))
    public abstract Service getServiceOnHost(@SQLParam("host_id") UUID hostId, @SQLParam("name") String name);
    
    @SQLGetter(table = Service.class, name = "get_service_on_host_by_name", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.service WHERE host_id = (SELECT h.id FROM bergamot.host h WHERE h.site_id = p_site_id AND h.name = p_host_name) AND name = p_name")
    )
    public abstract Service getServiceOnHostByName(@SQLParam(value = "site_id", virtual = true) UUID siteId, @SQLParam(value = "host_name", virtual = true) String hostName, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Service.class, name = "get_services_in_group", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.service WHERE group_ids @> ARRAY[p_group_id]")
    )
    public abstract List<Service> getServicesInGroup(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    @Cacheable
    @SQLGetter(table = Service.class, name = "list_services", since = @SQLVersion({1, 0, 0}))
    public abstract List<Service> listServices(@SQLParam("site_id") UUID siteId);
    
    @SQLGetter(table = Service.class, name = "list_services_that_are_not_ok", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT c.* FROM bergamot.service c JOIN bergamot.check_state s ON (c.id = s.check_id) WHERE c.site_id = p_site_id AND (NOT s.ok) AND s.hard AND (NOT c.suppressed)")
    )
    public abstract List<Service> listServicesThatAreNotOk(@SQLParam("site_id") UUID siteId);
    
    @SQLGetter(table = Service.class, name = "list_services_in_pool", since = @SQLVersion({1, 0, 0}))
    public abstract List<Service> listServicesInPool(@SQLParam("site_id") UUID siteId, @SQLParam("pool") int pool);
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}"})
    @SQLRemove(table = Service.class, name = "remove_service", since = @SQLVersion({1, 0, 0}))
    public abstract void removeService(@SQLParam("id") UUID id);
    
    // trap
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}"})
    @SQLSetter(table = Trap.class, name = "set_trap", since = @SQLVersion({1, 0, 0}))
    public abstract void setTrap(Trap trap);
    
    @Cacheable
    @SQLGetter(table = Trap.class, name = "get_trap", since = @SQLVersion({1, 0, 0}))
    public abstract Trap getTrap(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Trap.class, name = "get_traps_on_host", since = @SQLVersion({1, 0, 0}))
    public abstract List<Trap> getTrapsOnHost(@SQLParam("host_id") UUID hostId);
    
    @SQLGetter(table = Trap.class, name = "get_trap_on_host", since = @SQLVersion({1, 0, 0}))
    public abstract Trap getTrapOnHost(@SQLParam("host_id") UUID hostId, @SQLParam("name") String name);
    
    @SQLGetter(table = Trap.class, name = "get_trap_on_host_by_name", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.trap WHERE host_id = (SELECT h.id FROM bergamot.host h WHERE h.site_id = p_site_id AND h.name = p_host_name) AND name = p_name")
    )
    public abstract Trap getTrapOnHostByName(@SQLParam(value = "site_id", virtual = true) UUID siteId, @SQLParam(value = "host_name", virtual = true) String hostName, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Trap.class, name = "get_traps_in_group", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.trap WHERE group_ids @> ARRAY[p_group_id]")
    )
    public abstract List<Trap> getTrapsInGroup(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    @SQLGetter(table = Trap.class, name = "list_traps", since = @SQLVersion({1, 0, 0}))
    public abstract List<Trap> listTraps(@SQLParam("site_id") UUID siteId);
    
    @SQLGetter(table = Trap.class, name = "list_traps_that_are_not_ok", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT c.* FROM bergamot.trap c JOIN bergamot.check_state s ON (c.id = s.check_id) WHERE c.site_id = p_site_id AND (NOT s.ok) AND s.hard AND (NOT c.suppressed)")
    )
    public abstract List<Trap> listTrapsThatAreNotOk(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}"})
    @SQLRemove(table = Trap.class, name = "remove_trap", since = @SQLVersion({1, 0, 0}))
    public abstract void removeTrap(@SQLParam("id") UUID id);
    
    // cluster
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}", "get_cluster_by_name.#{site_id}.*"})
    @SQLSetter(table = Cluster.class, name = "set_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract void setCluster(Cluster cluster);
    
    @Cacheable
    @SQLGetter(table = Cluster.class, name = "get_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract Cluster getCluster(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Cluster.class, name = "get_cluster_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Cluster getClusterByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Cluster.class, name = "get_clusters_in_group", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.cluster WHERE group_ids @> ARRAY[p_group_id]")
    )
    public abstract List<Cluster> getClustersInGroup(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}", "get_cluster_by_name.#{this.getSiteId(id)}.*", "get_clusters_referencing_check.*"})
    @SQLRemove(table = Cluster.class, name = "remove_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract void removeCluster(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Cluster.class, name = "get_clusters_referencing_check", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.cluster WHERE reference_ids @> ARRAY[p_check_id]")
    )
    public abstract List<Cluster> getClustersReferencingCheck(@SQLParam(value = "check_id", virtual = true) UUID checkId);
    
    @SQLGetter(table = Cluster.class, name = "list_clusters", since = @SQLVersion({1, 0, 0}))
    public abstract List<Cluster> listClusters(@SQLParam("site_id") UUID siteId);
    
    @SQLGetter(table = Cluster.class, name = "list_clusters_that_are_not_ok", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT c.* FROM bergamot.cluster c JOIN bergamot.check_state s ON (c.id = s.check_id) WHERE c.site_id = p_site_id AND (NOT s.ok) AND s.hard AND (NOT c.suppressed)")
    )
    public abstract List<Cluster> listClustersThatAreNotOk(@SQLParam("site_id") UUID siteId);
    
    public void addResourceToCluster(Cluster cluster, Resource resource)
    {
        resource.setClusterId(cluster.getId());
        this.setResource(resource);
        this.getAdapterCache().remove("get_resources_on_cluster." + cluster.getId());
    }
    
    public void removeResourceFromCluster(Cluster cluster, Resource resource)
    {
        resource.setClusterId(null);
        this.setResource(resource);
        this.getAdapterCache().remove("get_resources_on_cluster." + cluster.getId());
    }
    
    // resources
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}", "get_resources_referencing_check.*"})
    @SQLSetter(table = Resource.class, name = "set_resource", since = @SQLVersion({1, 0, 0}))
    public abstract void setResource(Resource resource);
    
    @Cacheable
    @SQLGetter(table = Resource.class, name = "get_resource", since = @SQLVersion({1, 0, 0}))
    public abstract Resource getResource(@SQLParam("id") UUID id);
    
    @SQLGetter(table = Resource.class, name = "get_resources_on_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract List<Resource> getResourcesOnCluster(@SQLParam("cluster_id") UUID clusterId);
    
    @SQLGetter(table = Resource.class, name = "get_resource_on_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract Resource getResourceOnCluster(@SQLParam("cluster_id") UUID clusterId, @SQLParam("name") String name);
    
    @SQLGetter(table = Resource.class, name = "get_resource_on_cluster_by_name", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.resource WHERE host_id = (SELECT c.id FROM bergamot.cluster c WHERE c.site_id = p_site_id AND c.name = p_cluster_name) AND name = p_name")
    )
    public abstract Resource getResourceOnClusterByName(@SQLParam(value = "site_id", virtual = true) UUID siteId, @SQLParam(value = "cluster_name", virtual = true) String clusterName, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Resource.class, name = "get_resources_in_group", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.resource WHERE group_ids @> ARRAY[p_group_id]")
    )
    public abstract List<Resource> getResourcesInGroup(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    @Cacheable
    @CacheInvalidate({"check_command.#{id}", "check_state.#{id}", "get_resources_referencing_check.*"})
    @SQLRemove(table = Resource.class, name = "remove_resource", since = @SQLVersion({1, 0, 0}))
    public abstract void removeResource(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Resource.class, name = "get_resources_referencing_check", since = @SQLVersion({1, 0, 0}),
        query = @SQLQuery("SELECT * FROM bergamot.resource WHERE reference_ids @> ARRAY[p_check_id]")
    )
    public abstract List<Resource> getResourcesReferencingCheck(@SQLParam(value = "check_id", virtual = true) UUID checkId);
    
    @SQLGetter(table = Resource.class, name = "list_resources", since = @SQLVersion({1, 0, 0}))
    public abstract List<Resource> listResources(@SQLParam("site_id") UUID siteId);
    
    @SQLGetter(table = Resource.class, name = "list_resources_that_are_not_ok", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT c.* FROM bergamot.resource c JOIN bergamot.check_state s ON (c.id = s.check_id) WHERE c.site_id = p_site_id AND (NOT s.ok) AND s.hard AND (NOT c.suppressed)")
    )
    public abstract List<Resource> listResourceThatAreNotOk(@SQLParam("site_id") UUID siteId);
    
    // comments
    
    @Cacheable
    @CacheInvalidate({"get_comments_for_object.#{object_id}"})
    @SQLSetter(table = Comment.class, name = "set_comment", since = @SQLVersion({1, 0, 0}))
    public abstract void setComment(Comment comment);
    
    @Cacheable
    @SQLGetter(table = Comment.class, name = "get_comment", since = @SQLVersion({1, 0, 0}))
    public abstract Comment getComment(@SQLParam("id") UUID id);
    
    @Cacheable
    @CacheInvalidate({"get_comments_for_object.*"})
    @SQLRemove(table = Comment.class, name = "remove_comment", since = @SQLVersion({1, 0, 0}))
    public abstract void removeComment(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Comment.class, name = "get_comments_for_object", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder(value = "created", direction = Direction.DESC))
    public abstract List<Comment> getCommentsForObject(@SQLParam("object_id") UUID checkId, @SQLOffset long offset, @SQLLimit long limit);
    
    @SQLGetter(table = Comment.class, name = "list_comments", since = @SQLVersion({1, 0, 0}))
    public abstract List<Comment> listComments(@SQLParam("site_id") UUID siteId, @SQLOffset long offset, @SQLLimit long limit);
    
    // downtime
    
    @Cacheable
    @CacheInvalidate({"get_downtimes_for_check.#{check_id}", "get_all_downtimes_for_check.#{check_id}"})
    @SQLSetter(table = Downtime.class, name = "set_downtime", since = @SQLVersion({1, 0, 0}))
    public abstract void setDowntime(Downtime downtime);
    
    @Cacheable
    @SQLGetter(table = Downtime.class, name = "get_downtime", since = @SQLVersion({1, 0, 0}))
    public abstract Downtime getDowntime(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLRemove(table = Downtime.class, name = "remove_downtime", since = @SQLVersion({1, 0, 0}))
    public abstract void removeDowntime(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Downtime.class, name = "get_downtimes_for_check", since = @SQLVersion({1, 1, 1}), orderBy = @SQLOrder(value = "starts", direction = Direction.DESC),
        query = @SQLQuery("SELECT * FROM bergamot.downtime WHERE check_id = p_check_id AND starts >= (now() - p_past_interval::INTERVAL) AND ends <= (now() + p_future_interval::INTERVAL)")
    )
    public abstract List<Downtime> getDowntimesForCheck(@SQLParam("check_id") UUID checkId, @SQLParam(value = "past_interval", virtual = true) String pastInterval, @SQLParam(value = "future_interval", virtual = true) String futureInterval);
    
    public List<Downtime> getDowntimesForCheck(UUID checkId)
    {
        return this.getDowntimesForCheck(checkId, "1 week", "1 week");
    }
    
    @Cacheable
    @SQLGetter(table = Downtime.class, name = "get_all_downtimes_for_check", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder(value = "created", direction = Direction.DESC))
    public abstract List<Downtime> getAllDowntimesForCheck(@SQLParam("check_id") UUID checkId);
    
    @SQLGetter(table = Downtime.class, name = "list_downtimes", since = @SQLVersion({1, 0, 0}))
    public abstract List<Downtime> listDowntimes(@SQLParam("site_id") UUID siteId, @SQLOffset long offset, @SQLLimit long limit);
    
    // generic
    
    public void setCheck(Check<?, ?> check)
    {
        if (check instanceof Host)
            this.setHost((Host) check);
        else if (check instanceof Service)
            this.setService((Service) check);
        else if (check instanceof Trap)
            this.setTrap((Trap) check);
        else if (check instanceof Cluster)
            this.setCluster((Cluster) check);
        else if (check instanceof Resource)
            this.setResource((Resource) check);
        else if (check != null)
            throw new IllegalArgumentException("Cannot set check: " + check.getClass());
    }
    
    public void addContactToCheck(Check<?, ?> check, Contact contact)
    {
        if (! check.getContactIds().contains(contact.getId()))
        {
            check.getContactIds().add(contact.getId());
        }
        this.setCheck(check);
    }
    
    public void removeContactFromCheck(Check<?, ?> check, Contact contact)
    {
        check.getContactIds().remove(contact.getId());
        this.setCheck(check);
    }
    
    public void addTeamToCheck(Check<?, ?> check, Team team)
    {
        if (! check.getTeamIds().contains(team.getId()))
        {
            check.getTeamIds().add(team.getId());
        }
        this.setCheck(check);
    }
    
    public void removeTeamFromCheck(Check<?, ?> check, Team team)
    {
        check.getTeamIds().remove(team.getId());
        this.setCheck(check);
    }
    
    public Check<?,?> getCheck(UUID id)
    {
        Check<?,?> c = null;
        // service
        c = this.getService(id);
        if (c != null) return c;
        // trap
        c = this.getTrap(id);
        if (c != null) return c;
        // host
        c = this.getHost(id);
        if (c != null) return c;
        // resource
        c = this.getResource(id);
        if (c != null) return c;
        // cluster
        c = this.getCluster(id);
        if (c != null) return c;
        return c;
    }
    
    public List<Check<?,?>> getChecksInGroup(UUID groupId)
    {
        List<Check<?,?>> checks = new LinkedList<Check<?,?>>();
        // hosts
        checks.addAll(this.getHostsInGroup(groupId));
        // services
        checks.addAll(this.getServicesInGroup(groupId));
        // traps
        checks.addAll(this.getTrapsInGroup(groupId));
        // clusters
        checks.addAll(this.getClustersInGroup(groupId));
        // resources
        checks.addAll(this.getResourcesInGroup(groupId));
        return checks;
    }
    
    public List<VirtualCheck<?,?>> getVirtualChecksReferencingCheck(UUID checkId)
    {
        List<VirtualCheck<?,?>> r= new LinkedList<VirtualCheck<?,?>>();
        // resources
        r.addAll(this.getResourcesReferencingCheck(checkId));
        // clusters
        r.addAll(this.getClustersReferencingCheck(checkId));
        return r;
    }
    
    public List<Check<?,?>> listChecksThatAreNotOk(UUID siteId)
    {
        List<Check<?,?>> checks = new LinkedList<Check<?,?>>();
        // hosts
        checks.addAll(this.listHostsThatAreNotOk(siteId));
        // services
        checks.addAll(this.listServicesThatAreNotOk(siteId));
        // traps
        checks.addAll(this.listTrapsThatAreNotOk(siteId));
        // clusters
        checks.addAll(this.listClustersThatAreNotOk(siteId));
        // resources
        checks.addAll(this.listResourceThatAreNotOk(siteId));
        return checks;
    }
    
    //
    
    public VirtualCheckExpressionParserContext createVirtualCheckContext(final UUID siteId)
    {      
        return new VirtualCheckExpressionParserContext()
        {            
            @Override
            public Host lookupHost(String name)
            {
                return getHostByName(siteId, name);
            }

            @Override
            public Host lookupHost(UUID id)
            {
                return getHost(id);
            }

            @Override
            public Cluster lookupCluster(String name)
            {
                return getClusterByName(siteId, name);
            }

            @Override
            public Cluster lookupCluster(UUID id)
            {
                return getCluster(id);
            }

            @Override
            public Service lookupService(Host on, String name)
            {
                return getServiceOnHost(on.getId(), name);
            }

            @Override
            public Service lookupService(UUID id)
            {
                return getService(id);
            }

            @Override
            public Trap lookupTrap(Host on, String name)
            {
                return getTrapOnHost(on.getId(), name);
            }

            @Override
            public Trap lookupTrap(UUID id)
            {
                return getTrap(id);
            }

            @Override
            public Resource lookupResource(Cluster on, String name)
            {
                return getResourceOnCluster(on.getId(), name);
            }

            @Override
            public Resource lookupResource(UUID id)
            {
                return getResource(id);
            }           
        };
    }
    
    // helpers
    
    /**
     * Get the site id from a given object id
     */
    public UUID getSiteId(UUID objectId)
    {
        return Site.getSiteId(objectId);
    }
}
