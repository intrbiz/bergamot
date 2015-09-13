package com.intrbiz.bergamot.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.TemplatedObjectCfg;
import com.intrbiz.bergamot.config.validator.BergamotConfigResolver;
import com.intrbiz.bergamot.config.validator.BergamotObjectLocator;
import com.intrbiz.bergamot.model.APIToken;
import com.intrbiz.bergamot.model.AccessControl;
import com.intrbiz.bergamot.model.AgentRegistration;
import com.intrbiz.bergamot.model.Alert;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.CheckCommand;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Comment;
import com.intrbiz.bergamot.model.ComputedPermission;
import com.intrbiz.bergamot.model.ComputedPermissionForDomain;
import com.intrbiz.bergamot.model.Config;
import com.intrbiz.bergamot.model.ConfigChange;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Downtime;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.NotificationEngine;
import com.intrbiz.bergamot.model.Notifications;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.SecurityDomain;
import com.intrbiz.bergamot.model.SecurityDomainMembership;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.VirtualCheck;
import com.intrbiz.bergamot.model.state.CheckSavedState;
import com.intrbiz.bergamot.model.state.CheckState;
import com.intrbiz.bergamot.model.state.CheckStats;
import com.intrbiz.bergamot.model.state.CheckTransition;
import com.intrbiz.bergamot.model.state.GroupState;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;
import com.intrbiz.configuration.Configuration;
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
import com.intrbiz.data.db.compiler.meta.SQLPatch;
import com.intrbiz.data.db.compiler.meta.SQLQuery;
import com.intrbiz.data.db.compiler.meta.SQLRemove;
import com.intrbiz.data.db.compiler.meta.SQLSchema;
import com.intrbiz.data.db.compiler.meta.SQLSetter;
import com.intrbiz.data.db.compiler.meta.SQLVersion;
import com.intrbiz.data.db.compiler.meta.ScriptType;
import com.intrbiz.data.db.compiler.util.SQLScript;
import com.intrbiz.gerald.witchcraft.Witchcraft;

@SQLSchema(
        name = "bergamot", 
        version = @SQLVersion({3, 18, 0}),
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
            Downtime.class,
            APIToken.class,
            ConfigChange.class,
            CheckStats.class,
            CheckTransition.class,
            AgentRegistration.class,
            CheckSavedState.class,
            SecurityDomain.class,
            SecurityDomainMembership.class,
            AccessControl.class,
            ComputedPermission.class,
            ComputedPermissionForDomain.class
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
    
    public static void load()
    {
        // do nothing
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
    
    public void flushGlobalCaches()
    {
        this.getAdapterCache().removePrefix("get_site_by_name");
        this.getAdapterCache().removePrefix("get_site");
    }

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
    
    @SQLGetter(table = Config.class, name = "list_dependent_config", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.config WHERE site_id = p_site_id AND required_templates @> ARRAY[p_qualified_template_name] AND (NOT (type = 'service' OR type = 'trap' OR type = 'resource'))")
    )
    public abstract List<Config> listDependentConfig(@SQLParam("site_id") UUID siteId, @SQLParam(value = "qualified_template_name", virtual = true) String qualifiedTemplateName);
    
    /**
     * Recursively find all objects which utilise the given template
     */
    @SQLGetter(table = Config.class, name = "list_all_dependent_config_objects", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("WITH RECURSIVE config_graph AS ( " + 
                              "    SELECT c.* " +
                              "    FROM bergamot.config c " + 
                              "    WHERE c.site_id = p_site_id AND c.required_templates @> ARRAY[p_qualified_template_name] " +
                              "    UNION  " +
                              "    SELECT c.* " +
                              "    FROM bergamot.config c, config_graph cg " + 
                              "    WHERE c.site_id = p_site_id AND c.required_templates @> ARRAY[cg.type || ':' || cg.name] " +
                              "    ) " +
                              "    SELECT * " +
                              "    FROM config_graph cg " +
                              "    WHERE cg.template = FALSE AND (NOT (type = 'service' OR type = 'trap' OR type = 'resource'))")
    )
    public abstract List<Config> listAllDependentConfigObjects(@SQLParam("site_id") UUID siteId, @SQLParam(value = "qualified_template_name", virtual = true) String qualifiedTemplateName);
    
        
    
    public BergamotObjectLocator getObjectLocator(final UUID siteId)
    {
        return new BergamotObjectLocator()
        {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends TemplatedObjectCfg<T>> T lookup(Class<T> type, String name)
            {
                return (T) Util.nullable(BergamotDB.this.getConfigByName(siteId, Configuration.getRootElement(type), name), Config::getConfiguration);
            }
        };
    }
    
    public BergamotConfigResolver getConfigResolver(final UUID siteId)
    {
        return new BergamotConfigResolver(this.getObjectLocator(siteId));
    }
    
    @SQLSetter(table = ConfigChange.class, name = "set_config_change", since = @SQLVersion({1, 0, 0}))
    public abstract void setConfigChange(ConfigChange change);
    
    @SQLGetter(table = ConfigChange.class, name = "get_config_change", since = @SQLVersion({1, 0, 0}))
    public abstract ConfigChange getConfigChange(@SQLParam("id") UUID id);
    
    @SQLRemove(table = ConfigChange.class, name = "remove_config_change", since = @SQLVersion({1, 0, 0}))
    public abstract void removeConfigChange(@SQLParam("id") UUID id);
    
    @SQLGetter(table = ConfigChange.class, name = "list_config_changes", since = @SQLVersion({1, 0, 0}), 
            orderBy = { @SQLOrder(value = "applied", direction = Direction.ASC), @SQLOrder(value = "created", direction = Direction.DESC) }
    )
    public abstract List<ConfigChange> listConfigChanges(@SQLParam("site_id") UUID siteId);
    
    @SQLGetter(table = ConfigChange.class, name = "list_pending_config_changes", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.config_change WHERE site_id = p_site_id AND applied = FALSE"),
            orderBy = @SQLOrder(value = "created", direction = Direction.DESC)
    )
    public abstract List<ConfigChange> getPendingConfigChanges(@SQLParam("site_id") UUID siteId);
    
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
    @CacheInvalidate({
        "get_root_locations.#{site_id}", 
        "get_location_by_name.#{site_id}.*", 
        "get_locations_in_location.#{id}"
    })
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
    @CacheInvalidate({
        "get_root_locations.#{this.getSiteId(id)}", 
        "get_location_by_name.#{this.getSiteId(id)}.*", 
        "get_locations_in_location.#{id}"
    })
    @SQLRemove(table = Location.class, name = "remove_location", since = @SQLVersion({1, 0, 0}))
    public abstract void removeLocation(@SQLParam("id") UUID locationId);
    
    public void addLocationChild(Location parent, Location child)
    {
        child.setLocationId(parent.getId());
        this.setLocation(child);
        this.invalidateLocationsInLocation(parent.getId());
    }
    
    public void removeLocationChild(Location parent, Location child)
    {
        child.setLocationId(null);
        this.setLocation(child);
        this.invalidateLocationsInLocation(parent.getId());
    }
    
    public void invalidateLocationsInLocation(UUID locationId)
    {
        this.getAdapterCache().remove("get_locations_in_location." + locationId);
    }
    
    public void addLocationHost(Location location, Host host)
    {
        host.setLocationId(location.getId());
        this.setHost(host);
        this.invalidateHostsInLocation(location.getId());
    }
    
    public void removeLocationHost(Location location, Host host)
    {
        host.setLocationId(null);
        this.setHost(host);
        this.invalidateHostsInLocation(location.getId());
    }
    
    public void invalidateHostsInLocation(UUID locationId)
    {
        this.getAdapterCache().remove("get_hosts_in_location." + locationId);
    }
    
    // group
    
    @Cacheable
    @CacheInvalidate({
        "get_group_by_name.#{site_id}.*", 
        "get_root_groups.#{site_id}.*", 
        "get_groups_in_group.#{id}"
    })
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
    @CacheInvalidate({
        "get_group_by_name.#{this.getSiteId(id)}.*", 
        "get_root_groups.#{this.getSiteId(id)}.*", 
        "get_groups_in_group.#{id}"
    })
    @SQLRemove(table = Group.class, name = "remove_group", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery(
                        "UPDATE bergamot.group SET group_ids=array_remove(group_ids, p_id) WHERE group_ids @> ARRAY[p_id];\n" +
                        "UPDATE bergamot.contact  SET group_ids=array_remove(group_ids, p_id) WHERE group_ids @> ARRAY[p_id];\n" +
                        "UPDATE bergamot.host     SET group_ids=array_remove(group_ids, p_id) WHERE group_ids @> ARRAY[p_id];\n" +
                        "UPDATE bergamot.service  SET group_ids=array_remove(group_ids, p_id) WHERE group_ids @> ARRAY[p_id];\n" +
                        "UPDATE bergamot.trap     SET group_ids=array_remove(group_ids, p_id) WHERE group_ids @> ARRAY[p_id];\n" +
                        "UPDATE bergamot.cluster  SET group_ids=array_remove(group_ids, p_id) WHERE group_ids @> ARRAY[p_id];\n" +
                        "UPDATE bergamot.resource SET group_ids=array_remove(group_ids, p_id) WHERE group_ids @> ARRAY[p_id];\n" +
                        "DELETE FROM bergamot.group WHERE id = p_id"
            )
    )
    public abstract void removeGroup(@SQLParam("id") UUID id);
    
    public void addGroupChild(Group parent, Group child)
    {
        if (! child.getGroupIds().contains(parent.getId()))
        {
            child.getGroupIds().add(parent.getId());
        }   
        this.setGroup(child);
        this.invalidateGroupsInGroup(parent.getId());
    }
    
    public void removeGroupChild(Group parent, Group child)
    {
        child.getGroupIds().remove(parent.getId());
        this.setGroup(child);
        this.invalidateGroupsInGroup(parent.getId());
    }
    
    public void invalidateGroupsInGroup(UUID groupId)
    {
        this.getAdapterCache().remove("get_groups_in_group." + groupId);
    }
    
    public void addCheckToGroup(Group group, Check<?,?> check)
    {
        if (check.getGroupIds().contains(group.getId()))
        {
            check.getGroupIds().add(group.getId());
        }
        this.invalidateChecksInGroup(group.getId());
    }
    
    public void removeCheckFromGroup(Group group, Check<?,?> check)
    {
        check.getGroupIds().remove(group.getId());
        this.setCheck(check);
        this.invalidateChecksInGroup(group.getId());
    }
    
    public void invalidateChecksInGroup(UUID groupId)
    {
        this.getAdapterCache().remove("get_hosts_in_group." + groupId);
        this.getAdapterCache().remove("get_services_in_group." + groupId);
        this.getAdapterCache().remove("get_traps_in_group." + groupId);
        this.getAdapterCache().remove("get_clusters_in_group." + groupId);
        this.getAdapterCache().remove("get_resources_in_group." + groupId);        
    }
    
    // team
    
    @Cacheable
    @CacheInvalidate({
        "get_team_by_name.#{site_id}.*", 
        "get_teams_in_team.#{id}", 
        "get_contacts_in_team.#{id}"
    })
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
    @CacheInvalidate({
        "get_team_by_name.#{this.getSiteId(id)}.*", 
        "get_teams_in_team.#{id}", 
        "get_contacts_in_team.#{id}"
    })
    @SQLRemove(table = Team.class, name = "remove_team", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery(
                    "UPDATE bergamot.team     SET team_ids=array_remove(team_ids, p_id) WHERE team_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.contact  SET team_ids=array_remove(team_ids, p_id) WHERE team_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.host     SET team_ids=array_remove(team_ids, p_id) WHERE team_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.service  SET team_ids=array_remove(team_ids, p_id) WHERE team_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.trap     SET team_ids=array_remove(team_ids, p_id) WHERE team_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.cluster  SET team_ids=array_remove(team_ids, p_id) WHERE team_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.resource SET team_ids=array_remove(team_ids, p_id) WHERE team_ids @> ARRAY[p_id];\n" +
                    "DELETE FROM bergamot.team WHERE id = p_id"
            )
    )
    public abstract void removeTeam(@SQLParam("id") UUID id);
    
    public void addTeamChild(Team parent, Team child)
    {
        if (! child.getTeamIds().contains(parent.getId()))
        {
            child.getTeamIds().add(parent.getId());
        }
        this.setTeam(child);
        this.invalidateTeamsInTeam(parent.getId());
    }
    
    public void removeTeamChild(Team parent, Team child)
    {
        child.getTeamIds().remove(parent.getId());
        this.setTeam(child);
        this.invalidateTeamsInTeam(parent.getId());
    }
    
    public void invalidateTeamsInTeam(UUID teamId)
    {
        this.getAdapterCache().remove("get_teams_in_team." + teamId);
    }
    
    public void addContactToTeam(Team parent, Contact contact)
    {
        if (! contact.getTeamIds().contains(parent.getId()))
        {
            contact.getTeamIds().add(parent.getId());
        }
        this.setContact(contact);
        this.invalidateContactsInTeam(parent.getId());
    }
    
    public void removeContactFromTeam(Team parent, Contact contact)
    {
        contact.getTeamIds().remove(parent.getId());
        this.setContact(contact);
        this.invalidateContactsInTeam(parent.getId());
    }
    
    public void invalidateContactsInTeam(UUID teamId)
    {
        this.getAdapterCache().remove("get_contacts_in_team." + teamId);
    }
    
    // contact
    
    @Cacheable
    @CacheInvalidate({
        "get_contact_by_name.#{site_id}.*", 
        "get_contact_by_email.#{site_id}.*", 
        "get_contact_by_name_or_email.#{site_id}.*"
    })
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
            query = @SQLQuery("SELECT * FROM bergamot.contact WHERE site_id = p_site_id AND (name = p_name_or_email OR email = p_name_or_email)")
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
    @CacheInvalidate({
        "get_contact_by_name.#{this.getSiteId(id)}.*", 
        "get_contact_by_email.#{this.getSiteId(id)}.*", 
        "get_contact_by_name_or_email.#{this.getSiteId(id)}.*"
    })
    @SQLRemove(table = Contact.class, name = "remove_contact", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery(
                    "UPDATE bergamot.host     SET contact_ids=array_remove(contact_ids, p_id) WHERE contact_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.service  SET contact_ids=array_remove(contact_ids, p_id) WHERE contact_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.trap     SET contact_ids=array_remove(contact_ids, p_id) WHERE contact_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.cluster  SET contact_ids=array_remove(contact_ids, p_id) WHERE contact_ids @> ARRAY[p_id];\n" +
                    "UPDATE bergamot.resource SET contact_ids=array_remove(contact_ids, p_id) WHERE contact_ids @> ARRAY[p_id];\n" +
                    "DELETE FROM bergamot.contact WHERE id = p_id"
            )
    )
    public abstract void removeContact(@SQLParam("id") UUID id);
    
    // API tokens
    
    @Cacheable
    @SQLSetter(table = APIToken.class, name = "set_api_token", since = @SQLVersion({1, 0, 0}))
    public abstract void setAPIToken(APIToken token);
    
    @Cacheable
    @SQLGetter(table = APIToken.class, name = "get_api_token", since = @SQLVersion({1, 0, 0}))
    public abstract APIToken getAPIToken(@SQLParam("token") String token);
    
    @Cacheable
    @SQLRemove(table = APIToken.class, name = "remove_api_token", since = @SQLVersion({1, 0, 0}))
    public abstract void removeAPIToken(@SQLParam("token") String token);
    
    @SQLGetter(table = APIToken.class, name = "get_api_tokens_for_contact", since = @SQLVersion({1, 0, 0}))
    public abstract List<APIToken> getAPITokensForContact(@SQLParam("contact_id") UUID contactId);
    
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
    
    // saved state
    
    @Cacheable
    @SQLSetter(table = CheckSavedState.class, name = "set_check_saved_state", since = @SQLVersion({3, 7, 0}))
    public abstract void setCheckSavedState(CheckSavedState savedState);
    
    @Cacheable
    @SQLGetter(table = CheckSavedState.class, name = "get_check_saved_state", since = @SQLVersion({3, 7, 0}))
    public abstract CheckSavedState getCheckSavedState(@SQLParam("check_id") UUID id);
    
    @Cacheable
    @CacheInvalidate({
        "get_check_saved_state.#{check_id}",
    })
    @SQLRemove(table = CheckSavedState.class, name = "remove_check_saved_state", since = @SQLVersion({3, 7, 0}))
    public abstract void removeCheckSavedState(@SQLParam("check_id") UUID id);
    
    // alerts
    
    @Cacheable
    @CacheInvalidate({
        "get_all_alerts_for_check.#{check_id}", 
        "get_recovered_alerts_for_check.#{check_id}",
        "get_all_alerts_for_check_paged.#{check_id}.*",
        "get_alerts_for_check.#{check_id}", 
        "get_current_alert_for_check.#{check_id}"
    })
    @SQLSetter(table = Alert.class, name = "set_alert", since = @SQLVersion({1, 0, 0}))
    public abstract void setAlert(Alert alert);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_alert", since = @SQLVersion({1, 0, 0}))
    public abstract Alert getAlert(@SQLParam("id") UUID id);
    
    @Cacheable
    @CacheInvalidate({
        "get_all_alerts_for_check.*",
        "get_all_alerts_for_check_paged.*",
        "get_recovered_alerts_for_check.*", 
        "get_alerts_for_check.*", 
        "get_current_alert_for_check.*"
    })
    @SQLRemove(table = Alert.class, name = "remove_alert", since = @SQLVersion({1, 0, 0}))
    public abstract void removeAlert(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_all_alerts_for_check", since = @SQLVersion({1, 0, 0}), orderBy = @SQLOrder(value = "raised", direction = Direction.DESC))
    public abstract List<Alert> getAllAlertsForCheck(@SQLParam("check_id") UUID checkId);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_all_alerts_for_check_paged", since = @SQLVersion({3, 5, 0}), orderBy = @SQLOrder(value = "raised", direction = Direction.DESC))
    public abstract List<Alert> getAllAlertsForCheck(@SQLParam("check_id") UUID checkId, @SQLLimit() long limit, @SQLOffset() long offset);
    
    @Cacheable
    @SQLGetter(table = Alert.class, name = "get_all_recent_alerts_for_check", since = @SQLVersion({3, 5, 0}), orderBy = @SQLOrder(value = "raised", direction = Direction.DESC),
        query = @SQLQuery("SELECT * FROM bergamot.alert WHERE check_id = p_check_id AND raised > (now() - p_interval) ORDER BY raised DESC LIMIT p_limit")
    )
    public abstract List<Alert> getAllRecentAlertsForCheck(@SQLParam("check_id") UUID checkId, @SQLParam(value = "interval", virtual = true) String interval, @SQLLimit() long limit);
    
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
            query = @SQLQuery("SELECT * FROM bergamot.alert WHERE site_id = p_site_id AND recovered = FALSE AND acknowledged = FALSE ORDER BY raised DESC")
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
                              "  bool_and(s.ok OR s.suppressed OR s.in_downtime) AS ok, " +
                              "  max(CASE WHEN s.suppressed OR s.in_downtime THEN 0 ELSE s.status END)::INTEGER AS status, " +
                              "  count(CASE WHEN s.status = 0 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS pending_count, " +  
                              "  count(CASE WHEN s.status = 2 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS ok_count, " +
                              "  count(CASE WHEN s.status = 3 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS warning_count, " +
                              "  count(CASE WHEN s.status = 4 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS critical_count, " +
                              "  count(CASE WHEN s.status = 5 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS unknown_count, " +
                              "  count(CASE WHEN s.status = 6 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS timeout_count, " +
                              "  count(CASE WHEN s.status = 7 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS error_count, " +
                              "  count(CASE WHEN s.suppressed                                         THEN 1 ELSE NULL END)::INTEGER AS suppressed_count, " + 
                              "  count(CASE WHEN s.status = 1 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS info_count, " +
                              "  count(CASE WHEN s.status = 8 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS action_count, " +
                              "  count(CASE WHEN s.in_downtime                                        THEN 1 ELSE NULL END)::INTEGER AS in_downtime_count, " +  
                              "  count(s.check_id)::INTEGER                                                                          AS total_checks " +
                              "FROM bergamot.check_state s " +
                              "JOIN ( " +
                              "    SELECT id, group_ids FROM bergamot.host " +
                              "  UNION " + 
                              "    SELECT id, group_ids FROM bergamot.service " +
                              "  UNION  " +
                              "    SELECT id, group_ids FROM bergamot.trap " +
                              "  UNION " +
                              "    SELECT id, group_ids FROM bergamot.cluster " +
                              "  UNION " +
                              "    SELECT id, group_ids FROM bergamot.resource " +
                              ") q ON (s.check_id = q.id) " +
                              "JOIN group_graph g ON (q.group_ids @> ARRAY[g.id])")
    )
    public abstract GroupState computeGroupState(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    /**
     * Compute the state of the given group, with respect to the access controls 
     * of the given contact, this will recursively follow the group hierarchy 
     * an compute the state of all the checks within the group
     * @param groupId
     * @param contactId
     */
    @SQLGetter(table = GroupState.class, name = "compute_group_state_for_contact", since = @SQLVersion({3, 11, 0}),
        query = @SQLQuery(
                "WITH RECURSIVE group_graph(id) AS (  \n" +
                "    SELECT g.id  \n" +
                "    FROM bergamot.group g \n" +
                "    LEFT JOIN \n" +
                "    ( \n" +
                "      SELECT sdm1.check_id, coalesce(bool_or(cpfd1.allowed), false) as allowed \n" +
                "      FROM bergamot.security_domain_membership sdm1 \n" +
                "      JOIN bergamot.computed_permissions_for_domain cpfd1 ON (sdm1.security_domain_id = cpfd1.security_domain_id) \n" +
                "      WHERE cpfd1.permission = 'read' AND cpfd1.contact_id = p_contact_id \n" +
                "      GROUP BY sdm1.check_id \n" +
                "    ) q1 ON (q1.check_id = g.id) \n" +
                "    WHERE g.id = p_group_id AND (coalesce(q1.allowed, false) OR bergamot.has_permission(p_contact_id, 'read'))\n" +
                "  UNION  \n" +
                "    SELECT g.id  \n" +
                "    FROM bergamot.group g\n" +
                "    LEFT JOIN \n" +
                "    ( \n" +
                "      SELECT sdm2.check_id, coalesce(bool_or(cpfd2.allowed), false) as allowed \n" +
                "      FROM bergamot.security_domain_membership sdm2 \n" +
                "      JOIN bergamot.computed_permissions_for_domain cpfd2 ON (sdm2.security_domain_id = cpfd2.security_domain_id) \n" +
                "      WHERE cpfd2.permission = 'read' AND cpfd2.contact_id = p_contact_id \n" +
                "      GROUP BY sdm2.check_id \n" +
                "    ) q2 ON (q2.check_id = g.id), \n" +
                "    group_graph gg  \n" +
                "    WHERE g.group_ids @> ARRAY[gg.id] AND (coalesce(q2.allowed, false) OR bergamot.has_permission(p_contact_id, 'read'))\n" +
                ")  \n" +
                "SELECT   \n" +
                "  p_group_id,  \n" +
                "  coalesce(bool_and(s.ok OR s.suppressed OR s.in_downtime), true) AS ok,  \n" +
                "  coalesce(max(CASE WHEN s.suppressed OR s.in_downtime THEN 0 ELSE s.status END)::INTEGER, 0) AS status,  \n" +
                "  count(CASE WHEN s.status = 0 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS pending_count,    \n" +
                "  count(CASE WHEN s.status = 2 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS ok_count,  \n" +
                "  count(CASE WHEN s.status = 3 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS warning_count,  \n" +
                "  count(CASE WHEN s.status = 4 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS critical_count,  \n" +
                "  count(CASE WHEN s.status = 5 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS unknown_count,  \n" +
                "  count(CASE WHEN s.status = 6 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS timeout_count,  \n" +
                "  count(CASE WHEN s.status = 7 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS error_count,  \n" +
                "  count(CASE WHEN s.suppressed                                         THEN 1 ELSE NULL END)::INTEGER AS suppressed_count,   \n" +
                "  count(CASE WHEN s.status = 1 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS info_count,  \n" +
                "  count(CASE WHEN s.status = 8 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS action_count,  \n" +
                "  count(CASE WHEN s.in_downtime                                        THEN 1 ELSE NULL END)::INTEGER AS in_downtime_count,    \n" +
                "  count(s.check_id)::INTEGER                                                                          AS total_checks  \n" +
                "FROM\n" +
                " bergamot.check_state s  \n" +
                " JOIN (  \n" +
                "    SELECT id, group_ids FROM bergamot.host  \n" +
                "  UNION   \n" +
                "    SELECT id, group_ids FROM bergamot.service    \n" +
                "  UNION   \n" +
                "    SELECT id, group_ids FROM bergamot.trap  \n" +
                "  UNION  \n" +
                "    SELECT id, group_ids FROM bergamot.cluster  \n" +
                "  UNION  \n" +
                "    SELECT id, group_ids FROM bergamot.resource  \n" +
                " ) q ON (s.check_id = q.id)\n" +
                "JOIN group_graph g ON (q.group_ids @> ARRAY[g.id])\n" +
                "LEFT JOIN \n" +
                "( \n" +
                "  SELECT sdm3.check_id, coalesce(bool_or(cpfd3.allowed), false) as allowed \n" +
                "  FROM bergamot.security_domain_membership sdm3 \n" +
                "  JOIN bergamot.computed_permissions_for_domain cpfd3 ON (sdm3.security_domain_id = cpfd3.security_domain_id) \n" +
                "  WHERE cpfd3.permission = 'read' AND cpfd3.contact_id = p_contact_id \n" +
                "  GROUP BY sdm3.check_id \n" +
                ") q3 ON (q3.check_id = s.check_id) \n" +
                "WHERE coalesce(q3.allowed, false) OR bergamot.has_permission(p_contact_id, 'read')\n"
        )
    )
    public abstract GroupState computeGroupStateForContact(@SQLParam(value = "group_id", virtual = true) UUID groupId, @SQLParam(value = "contact_id", virtual = true) UUID contactId);
    
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
                              "  bool_and(s.ok OR s.suppressed OR s.in_downtime) AS ok, " +
                              "  max(CASE WHEN s.suppressed OR s.in_downtime THEN 0 ELSE s.status END)::INTEGER AS status, " +
                              "  count(CASE WHEN s.status = 0 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS pending_count, " +  
                              "  count(CASE WHEN s.status = 2 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS ok_count, " +
                              "  count(CASE WHEN s.status = 3 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS warning_count, " +
                              "  count(CASE WHEN s.status = 4 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS critical_count, " +
                              "  count(CASE WHEN s.status = 5 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS unknown_count, " +
                              "  count(CASE WHEN s.status = 6 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS timeout_count, " +
                              "  count(CASE WHEN s.status = 7 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS error_count, " +
                              "  count(CASE WHEN s.suppressed                                         THEN 1 ELSE NULL END)::INTEGER AS suppressed_count, " + 
                              "  count(CASE WHEN s.status = 1 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS info_count, " +
                              "  count(CASE WHEN s.status = 8 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS action_count, " +
                              "  count(CASE WHEN s.in_downtime                                        THEN 1 ELSE NULL END)::INTEGER AS in_downtime_count, " +  
                              "  count(s.check_id)::INTEGER                                                                          AS total_checks " +  
                              "FROM bergamot.check_state s " +
                              "JOIN bergamot.host h ON (s.check_id = h.id) "+
                              "JOIN location_graph lg ON (h.location_id = lg.id)")
    )
    public abstract GroupState computeLocationState(@SQLParam(value = "location_id", virtual = true) UUID locationId);
    
    /**
     * Compute the state of the given location taking into account the permissions 
     * granted to the given contact, this will recursively follow the location 
     * hierarchy an compute the state of all the checks within the location
     * @param groupId
     * @return
     */
    @SQLGetter(table = GroupState.class, name = "compute_location_state_for_contact", since = @SQLVersion({3, 13, 0}),
        query = @SQLQuery(
            "WITH RECURSIVE location_graph(id) AS (\n" +
            "    SELECT l1.id\n" +
            "    FROM bergamot.location l1\n" +
            "    LEFT JOIN \n" +
            "    ( \n" +
            "      SELECT sdm1.check_id, coalesce(bool_or(cpfd1.allowed), false) as allowed \n" +
            "      FROM bergamot.security_domain_membership sdm1 \n" +
            "      JOIN bergamot.computed_permissions_for_domain cpfd1 ON (sdm1.security_domain_id = cpfd1.security_domain_id) \n" +
            "      WHERE cpfd1.permission = 'read' AND cpfd1.contact_id = p_contact_id \n" +
            "      GROUP BY sdm1.check_id \n" +
            "    ) q1 ON (q1.check_id = l1.id) \n" +
            "    WHERE l1.id = p_location_id AND (coalesce(q1.allowed, false) OR bergamot.has_permission(p_contact_id, 'read'))\n" +
            "  UNION\n" +
            "    SELECT l2.id\n" +
            "    FROM bergamot.location l2\n" +
            "    LEFT JOIN \n" +
            "    ( \n" +
            "      SELECT sdm2.check_id, coalesce(bool_or(cpfd2.allowed), false) as allowed \n" +
            "      FROM bergamot.security_domain_membership sdm2 \n" +
            "      JOIN bergamot.computed_permissions_for_domain cpfd2 ON (sdm2.security_domain_id = cpfd2.security_domain_id) \n" +
            "      WHERE cpfd2.permission = 'read' AND cpfd2.contact_id = p_contact_id \n" +
            "      GROUP BY sdm2.check_id \n" +
            "    ) q2 ON (q2.check_id = l2.id), \n" +
            "    location_graph lg\n" +
            "    WHERE l2.location_id = lg.id AND (coalesce(q2.allowed, false) OR bergamot.has_permission(p_contact_id, 'read'))\n" +
            ") \n" +
            "SELECT\n" +
            "  p_location_id,\n" +
            "  coalesce(bool_and(s.ok OR s.suppressed OR s.in_downtime), true) AS ok, \n" +
            "  coalesce(max(CASE WHEN s.suppressed OR s.in_downtime THEN 0 ELSE s.status END)::INTEGER, 0) AS status, \n" +
            "  count(CASE WHEN s.status = 0 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS pending_count,   \n" +
            "  count(CASE WHEN s.status = 2 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS ok_count, \n" +
            "  count(CASE WHEN s.status = 3 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS warning_count, \n" +
            "  count(CASE WHEN s.status = 4 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS critical_count, \n" +
            "  count(CASE WHEN s.status = 5 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS unknown_count, \n" +
            "  count(CASE WHEN s.status = 6 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS timeout_count, \n" +
            "  count(CASE WHEN s.status = 7 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS error_count, \n" +
            "  count(CASE WHEN s.suppressed                                         THEN 1 ELSE NULL END)::INTEGER AS suppressed_count,  \n" +
            "  count(CASE WHEN s.status = 1 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS info_count, \n" +
            "  count(CASE WHEN s.status = 8 AND NOT (s.suppressed OR s.in_downtime) THEN 1 ELSE NULL END)::INTEGER AS action_count, \n" +
            "  count(CASE WHEN s.in_downtime                                        THEN 1 ELSE NULL END)::INTEGER AS in_downtime_count,   \n" +
            "  count(s.check_id)::INTEGER                                                                          AS total_checks   \n" +
            "FROM bergamot.check_state s \n" +
            "JOIN bergamot.host h ON (s.check_id = h.id)\n" +
            "JOIN location_graph lg ON (h.location_id = lg.id)\n" +
            "LEFT JOIN \n" +
            "( \n" +
            "  SELECT sdm3.check_id, coalesce(bool_or(cpfd3.allowed), false) as allowed \n" +
            "  FROM bergamot.security_domain_membership sdm3 \n" +
            "  JOIN bergamot.computed_permissions_for_domain cpfd3 ON (sdm3.security_domain_id = cpfd3.security_domain_id) \n" +
            "  WHERE cpfd3.permission = 'read' AND cpfd3.contact_id = p_contact_id \n" +
            "  GROUP BY sdm3.check_id \n" +
            ") q3 ON (q3.check_id = s.check_id) \n" +
            "WHERE coalesce(q3.allowed, false) OR bergamot.has_permission(p_contact_id, 'read')\n"
        )
    )
    public abstract GroupState computeLocationStateForContact(@SQLParam(value = "location_id", virtual = true) UUID locationId, @SQLParam(value = "contact_id", virtual = true) UUID contactId);
    
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
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}", 
        "get_host_by_name.#{site_id}.*", 
        "get_host_by_address.#{site_id}.*", 
        "get_host_by_external_ref.#{site_id}.*", 
        "get_host_by_agent_id.#{site_id}.*",
        "get_hosts_in_location.*",
        "get_hosts_in_group.*"
        
    })
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
    @SQLGetter(table = Host.class, name = "get_host_by_external_ref", since = @SQLVersion({2, 1, 0}))
    public abstract Host getHostByExternalRef(@SQLParam("site_id") UUID siteId, @SQLParam("external_ref") String externalRef);
    
    @Cacheable
    @SQLGetter(table = Host.class, name = "get_host_by_agent_id", since = @SQLVersion({2, 3, 0}))
    public abstract Host getHostByAgentId(@SQLParam("site_id") UUID siteId, @SQLParam("agent_id") UUID agentId);
    
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
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}", 
        "get_host_by_name.#{this.getSiteId(id)}.*", 
        "get_host_by_address.#{this.getSiteId(id)}.*",
        "get_host_by_external_ref.#{this.getSiteId(id)}.*", 
        "get_host_by_agent_id.#{this.getSiteId(id)}.*",
        "get_hosts_in_location.*",
        "get_hosts_in_group.*"
    })
    @SQLRemove(table = Host.class, name = "remove_host", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery(
                    "DELETE FROM bergamot.comment USING bergamot.alert a WHERE object_id = a.id AND a.check_id = p_id;\n" +
                    "DELETE FROM bergamot.comment                        WHERE object_id = p_id;\n" +
                    "DELETE FROM bergamot.alert                          WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_state                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_stats                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_transition               WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.downtime                       WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.notification_engine            WHERE notifications_id = p_id;\n" +
                    "DELETE FROM bergamot.notifications                  WHERE id = p_id;\n" +
                    "DELETE FROM bergamot.host                           WHERE id = p_id"
            )
    )
    public abstract void removeHost(@SQLParam("id") UUID id);
    
    public void addServiceToHost(Host host, Service service)
    {
        service.setHostId(host.getId());
        this.setService(service);
        this.invalidateServicesOnHost(host.getId());
    }
    
    public void removeServiceFromHost(Host host, Service service)
    {
        service.setHostId(null);
        this.setService(service);
        this.invalidateServicesOnHost(host.getId());
    }
    
    public void invalidateServicesOnHost(UUID hostId)
    {
        this.getAdapterCache().remove("get_services_on_host." + hostId);
    }
    
    public void addTrapToHost(Host host, Trap trap)
    {
        trap.setHostId(host.getId());
        this.setTrap(trap);
        this.invalidateTrapsOnHost(host.getId());
    }
    
    public void removeTrapFromHost(Host host, Trap trap)
    {
        trap.setHostId(null);
        this.setTrap(trap);
        this.invalidateTrapsOnHost(host.getId());
    }
    
    public void invalidateTrapsOnHost(UUID hostId)
    {
        this.getAdapterCache().remove("get_traps_on_host." + hostId);
    }
    
    // service
    
    @Cacheable
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}",
        "get_services_on_host.#{host_id}"
    })
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
    
    @SQLGetter(table = Service.class, name = "get_service_on_host_by_external_ref", since = @SQLVersion({2, 1, 0}))
    public abstract Service getServiceOnHostByExternalRef(@SQLParam("host_id") UUID hostId, @SQLParam("external_ref") String externalRef);
    
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
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}"
    })
    @SQLRemove(table = Service.class, name = "remove_service", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery(
                    "DELETE FROM bergamot.comment USING bergamot.alert a WHERE object_id = a.id AND a.check_id = p_id;\n" +
                    "DELETE FROM bergamot.comment                        WHERE object_id = p_id;\n" +
                    "DELETE FROM bergamot.alert                          WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_state                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_stats                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_transition               WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.downtime                       WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.notification_engine            WHERE notifications_id = p_id;\n" +
                    "DELETE FROM bergamot.notifications                  WHERE id = p_id;\n" +
                    "DELETE FROM bergamot.service                        WHERE id = p_id"
            )
    )
    public abstract void removeService(@SQLParam("id") UUID id);
    
    // trap
    
    @Cacheable
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}",
        "get_traps_on_host.#{host_id}"
    })
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
    
    @SQLGetter(table = Trap.class, name = "get_trap_on_host_by_external_ref", since = @SQLVersion({2, 1, 0}))
    public abstract Trap getTrapOnHostByExternalRef(@SQLParam("host_id") UUID hostId, @SQLParam("external_ref") String externalRef);
    
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
    
    @SQLGetter(table = Trap.class, name = "list_traps_for_watcher", since = @SQLVersion({1, 8, 0}),
            query = @SQLQuery("SELECT t.* FROM bergamot.trap t" +
                              " JOIN bergamot.check_command cc ON (t.id = cc.check_id)" +
                              " JOIN bergamot.command c ON (cc.command_id = c.id)" +
                              " JOIN bergamot.host h ON (t.host_id = h.id)" +
                              " WHERE (c.site_id = p_site_id OR p_site_id IS NULL) AND (h.location_id = p_location_id OR p_location_id IS NULL) AND (c.engine = p_engine OR p_engine IS NULL)")
    )
    public abstract List<Trap> listTrapsForWatcher(@SQLParam("site_id") UUID siteId, @SQLParam(value = "location_id", virtual = true) UUID locationId, @SQLParam(value = "engine", virtual = true) String engine);
    
    @SQLGetter(table = Trap.class, name = "list_traps_that_are_not_ok", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT c.* FROM bergamot.trap c JOIN bergamot.check_state s ON (c.id = s.check_id) WHERE c.site_id = p_site_id AND (NOT s.ok) AND s.hard AND (NOT c.suppressed)")
    )
    public abstract List<Trap> listTrapsThatAreNotOk(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}"
    })
    @SQLRemove(table = Trap.class, name = "remove_trap", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery(
                    "DELETE FROM bergamot.comment USING bergamot.alert a WHERE object_id = a.id AND a.check_id = p_id;\n" +
                    "DELETE FROM bergamot.comment                        WHERE object_id = p_id;\n" +
                    "DELETE FROM bergamot.alert                          WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_state                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_stats                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_transition               WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.downtime                       WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.notification_engine            WHERE notifications_id = p_id;\n" +
                    "DELETE FROM bergamot.notifications                  WHERE id = p_id;\n" +
                    "DELETE FROM bergamot.trap                           WHERE id = p_id"
            )
    )
    public abstract void removeTrap(@SQLParam("id") UUID id);
    
    // cluster
    
    @Cacheable
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}", 
        "get_cluster_by_name.#{site_id}.*"
    })
    @SQLSetter(table = Cluster.class, name = "set_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract void setCluster(Cluster cluster);
    
    @Cacheable
    @SQLGetter(table = Cluster.class, name = "get_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract Cluster getCluster(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = Cluster.class, name = "get_cluster_by_name", since = @SQLVersion({1, 0, 0}))
    public abstract Cluster getClusterByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = Cluster.class, name = "get_cluster_by_external_ref", since = @SQLVersion({2, 1, 0}))
    public abstract Cluster getClusterByExternalRef(@SQLParam("site_id") UUID siteId, @SQLParam("external_ref") String externalRef);
    
    @Cacheable
    @SQLGetter(table = Cluster.class, name = "get_clusters_in_group", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery("SELECT * FROM bergamot.cluster WHERE group_ids @> ARRAY[p_group_id]")
    )
    public abstract List<Cluster> getClustersInGroup(@SQLParam(value = "group_id", virtual = true) UUID groupId);
    
    @Cacheable
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}", 
        "get_cluster_by_name.#{this.getSiteId(id)}.*", 
        "get_clusters_referencing_check.*"
    })
    @SQLRemove(table = Cluster.class, name = "remove_cluster", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery(
                    "DELETE FROM bergamot.comment USING bergamot.alert a WHERE object_id = a.id AND a.check_id = p_id;\n" +
                    "DELETE FROM bergamot.comment                        WHERE object_id = p_id;\n" +
                    "DELETE FROM bergamot.alert                          WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_state                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_stats                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_transition               WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.downtime                       WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.notification_engine            WHERE notifications_id = p_id;\n" +
                    "DELETE FROM bergamot.notifications                  WHERE id = p_id;\n" +
                    "DELETE FROM bergamot.cluster                        WHERE id = p_id"
            )
    )
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
        this.invalidateResourcesOnCluster(cluster.getId());
    }
    
    public void removeResourceFromCluster(Cluster cluster, Resource resource)
    {
        resource.setClusterId(null);
        this.setResource(resource);
        this.invalidateResourcesOnCluster(cluster.getId());
    }
    
    public void invalidateResourcesOnCluster(UUID clusterId)
    {
        this.getAdapterCache().remove("get_resources_on_cluster." + clusterId);
    }
    
    // resources
    
    @Cacheable
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}", 
        "get_resources_referencing_check.*",
        "get_resources_on_cluster.#{cluster_id}"
    })
    @SQLSetter(table = Resource.class, name = "set_resource", since = @SQLVersion({1, 0, 0}))
    public abstract void setResource(Resource resource);
    
    @Cacheable
    @SQLGetter(table = Resource.class, name = "get_resource", since = @SQLVersion({1, 0, 0}))
    public abstract Resource getResource(@SQLParam("id") UUID id);
    
    @SQLGetter(table = Resource.class, name = "get_resources_on_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract List<Resource> getResourcesOnCluster(@SQLParam("cluster_id") UUID clusterId);
    
    @SQLGetter(table = Resource.class, name = "get_resource_on_cluster", since = @SQLVersion({1, 0, 0}))
    public abstract Resource getResourceOnCluster(@SQLParam("cluster_id") UUID clusterId, @SQLParam("name") String name);
    
    @SQLGetter(table = Resource.class, name = "get_resource_on_cluster_by_external_ref", since = @SQLVersion({2, 1, 0}))
    public abstract Resource getResourceOnClusterByExternalRef(@SQLParam("cluster_id") UUID clusterId, @SQLParam("external_ref") String externalRef);
    
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
    @CacheInvalidate({
        "check_command.#{id}", 
        "check_state.#{id}", 
        "get_resources_referencing_check.*"
    })
    @SQLRemove(table = Resource.class, name = "remove_resource", since = @SQLVersion({1, 0, 0}),
            query = @SQLQuery(
                    "DELETE FROM bergamot.comment USING bergamot.alert a WHERE object_id = a.id AND a.check_id = p_id;\n" +
                    "DELETE FROM bergamot.comment                        WHERE object_id = p_id;\n" +
                    "DELETE FROM bergamot.alert                          WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_state                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_stats                    WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.check_transition               WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.downtime                       WHERE check_id = p_id;\n" +
                    "DELETE FROM bergamot.notification_engine            WHERE notifications_id = p_id;\n" +
                    "DELETE FROM bergamot.notifications                  WHERE id = p_id;\n" +
                    "DELETE FROM bergamot.resource                       WHERE id = p_id"
            )
    )
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
    @CacheInvalidate({"get_comments_for_object.*"})
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
    @CacheInvalidate({"get_downtimes_for_check.#{check_id}.*", "get_all_downtimes_for_check.#{check_id}"})
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
    
    // stats
    
    @Cacheable
    @SQLSetter(table = CheckStats.class, name = "set_check_stats", since = @SQLVersion({1, 0, 0}))
    public abstract void setCheckStats(CheckStats state);
    
    @Cacheable
    @SQLGetter(table = CheckStats.class, name = "get_check_stats", since = @SQLVersion({1, 0, 0}))
    public abstract CheckStats getCheckStats(@SQLParam("check_id") UUID id);
    
    @Cacheable
    @SQLRemove(table = CheckStats.class, name = "remove_check_stats", since = @SQLVersion({1, 0, 0}))
    public abstract void removeCheckStats(@SQLParam("check_id") UUID id);
    
    // transition log
    
    @SQLSetter(table = CheckTransition.class, name = "log_check_transition", since = @SQLVersion({1, 3, 0}))
    public abstract void logCheckTransition(CheckTransition transition);
    
    @SQLGetter(table = CheckTransition.class, name = "get_check_transition", since = @SQLVersion({1, 3, 0}))
    public abstract CheckTransition getCheckTransition(@SQLParam("id") UUID id);
    
    @SQLGetter(table = CheckTransition.class, name = "list_check_transitions_for_check", since = @SQLVersion({1, 3, 0}), orderBy = @SQLOrder(value = "applied_at", direction = Direction.DESC))
    public abstract List<CheckTransition> listCheckTransitionsForCheck(@SQLParam("check_id") UUID checkId, @SQLOffset long offset, @SQLLimit long limit);
    
    @SQLGetter(table = CheckTransition.class, name = "list_check_transitions_for_check", since = @SQLVersion({1, 3, 0}), orderBy = @SQLOrder(value = "applied_at", direction = Direction.DESC),
            query = @SQLQuery("SELECT * FROM bergamot.check_transition WHERE check_id = p_check_id AND applied_at BETWEEN p_from AND p_to")
    )
    public abstract List<CheckTransition> listCheckTransitionsForCheckByDate(@SQLParam("check_id") UUID checkId, @SQLParam(value = "from", virtual = true) Timestamp from, @SQLParam(value = "to", virtual = true) Timestamp to);
    
    @SQLRemove(table = CheckTransition.class, name = "remove_check_Transition", since = @SQLVersion({1, 3, 0}))
    public abstract void removeCheckTransition(@SQLParam("id") UUID id);
    
    // agent
    
    @SQLSetter(table = AgentRegistration.class, name = "set_agent_registration", since = @SQLVersion({2, 2, 0}))
    public abstract void setAgentRegistration(AgentRegistration reg);
    
    @SQLGetter(table = AgentRegistration.class, name = "get_agent_registration", since = @SQLVersion({2, 2, 0}))
    public abstract AgentRegistration getAgentRegistration(@SQLParam("id") UUID id);
    
    @SQLGetter(table = AgentRegistration.class, name ="get_agent_registration_by_name", since = @SQLVersion({2, 2, 0}))
    public abstract AgentRegistration getAgentRegistrationByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @SQLRemove(table = AgentRegistration.class, name = "remove_agent_registration", since = @SQLVersion({2, 2, 0}))
    public abstract void removeAgentRegistration(@SQLParam("id") UUID id);
    
    @SQLGetter(table = AgentRegistration.class, name = "list_agent_registrations", since = @SQLVersion({2, 2, 0}))
    public abstract List<AgentRegistration> listAgentRegistrations(@SQLParam("site_id") UUID siteId);
    
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
    
    // security domains
    
    @Cacheable
    @CacheInvalidate({
        "get_security_domain_by_name.#{this.getSiteId(id)}.*",
    })
    @SQLSetter(table = SecurityDomain.class, name = "set_security_domain", since = @SQLVersion({3, 8, 0}))
    public abstract void setSecurityDomain(SecurityDomain securityDomain);
    
    @Cacheable
    @SQLGetter(table = SecurityDomain.class, name = "get_security_domain", since = @SQLVersion({3, 8, 0}))
    public abstract SecurityDomain getSecurityDomain(@SQLParam("id") UUID id);
    
    @Cacheable
    @SQLGetter(table = SecurityDomain.class, name = "get_security_domain_by_name", since = @SQLVersion({3, 8, 0}))
    public abstract SecurityDomain getSecurityDomainByName(@SQLParam("site_id") UUID siteId, @SQLParam("name") String name);
    
    @Cacheable
    @SQLGetter(table = SecurityDomain.class, name = "list_security_domains", since = @SQLVersion({3, 17, 0}))
    public abstract List<SecurityDomain> listSecurityDomains(@SQLParam("site_id") UUID siteId);
    
    @Cacheable
    @CacheInvalidate({
        "get_security_domain_by_name.#{this.getSiteId(id)}.*",
    })
    @SQLRemove(table = SecurityDomain.class, name = "remove_security_domain", since = @SQLVersion({3, 8, 0}))
    public abstract void removeSecurityDomain(@SQLParam("id") UUID id);
    
    
    @Cacheable
    @CacheInvalidate({
        "get_security_domain_members.#{security_domain_id}",
        "get_security_domains_for_check.#{check_id}"
    })
    @SQLSetter(table = SecurityDomainMembership.class, name = "set_security_domain_membership", since = @SQLVersion({3, 8, 0}), upsert = false)
    public abstract void setSecurityDomainMembership(SecurityDomainMembership membership);
    
    @Cacheable
    @SQLGetter(table = SecurityDomainMembership.class, name = "get_security_domain_members", since = @SQLVersion({3, 8, 0}))
    public abstract List<SecurityDomainMembership> getSecurityDomainMembers(@SQLParam("security_domain_id") UUID securityDomainId);
    
    @Cacheable
    @CacheInvalidate({
        "get_security_domain_members.#{security_domain_id}",
        "get_security_domains_for_check.#{check_id}"
    })
    @SQLRemove(table = SecurityDomainMembership.class, name = "remove_security_domain_membership", since = @SQLVersion({3, 8, 0}))
    public abstract void removeSecurityDomainMembership(@SQLParam("security_domain_id") UUID securityDomainId, @SQLParam("check_id") UUID checkId);
    
    @Cacheable
    @CacheInvalidate({
        "get_security_domain_members.*",
        "get_security_domains_for_check.#{check_id}"
    })
    @SQLRemove(table = SecurityDomainMembership.class, name = "remove_security_domain_membership_for_check", since = @SQLVersion({3, 8, 0}))
    public abstract void removeSecurityDomainMembershipForCheck(@SQLParam("check_id") UUID checkId);
    
    
    @Cacheable
    @SQLGetter(table = SecurityDomain.class, name = "get_security_domains_for_object", since = @SQLVersion({3, 8, 0}),
        query = @SQLQuery(
                "SELECT sd.* "
                + "FROM bergamot.security_domain sd "
                + "JOIN bergamot.security_domain_membership sdm ON (sd.id = sdm.security_domain_id) "
                + "WHERE sdm.check_id = p_check_id"
        )
    )
    public abstract List<SecurityDomain> getSecurityDomainsForObject(@SQLParam(value = "check_id", virtual = true) UUID checkId);
    
    public List<Check<?,?>> getChecksInSecurityDomain(UUID securityDomainId)
    {
        List<Check<?,?>> checks = new LinkedList<Check<?,?>>();
        for (SecurityDomainMembership member : this.getSecurityDomainMembers(securityDomainId))
        {
            checks.add(this.getCheck(member.getCheckId()));
        }
        return checks;
    }
    
    public void addCheckToSecurityDomain(UUID securityDomainId, UUID checkId)
    {
        this.setSecurityDomainMembership(new SecurityDomainMembership(securityDomainId, checkId));
    }
    
    // access controls
    
    @Cacheable
    @SQLSetter(table = AccessControl.class, name = "set_access_control", since = @SQLVersion({3, 8, 0}))
    public abstract void setAccessControl(AccessControl accessControl);
    
    @Cacheable
    @SQLGetter(table = AccessControl.class, name = "get_access_control", since = @SQLVersion({3, 8, 0}))
    public abstract AccessControl getAccessControl(@SQLParam("security_domain_id") UUID securityDomainId, @SQLParam("role_id") UUID roleId);
    
    @Cacheable
    @SQLGetter(table = AccessControl.class, name = "get_access_controls_for_security_domain", since = @SQLVersion({3, 8, 0}))
    public abstract List<AccessControl> getAccessControlsForSecurityDomain(@SQLParam("security_domain_id") UUID securityDomainId);
    
    @Cacheable
    @SQLGetter(table = AccessControl.class, name = "get_access_controls_for_role", since = @SQLVersion({3, 8, 0}))
    public abstract List<AccessControl> getAccessControlsForRole(@SQLParam("role_id") UUID roleId);
    
    @Cacheable
    @SQLRemove(table = AccessControl.class, name = "remove_access_control", since = @SQLVersion({3, 8, 0}))
    public abstract void removeAccessControl(@SQLParam("security_domain_id") UUID securityDomainId, @SQLParam("role_id") UUID roleId);
    
    // compute permissions from ACL information
    
    private Timer build_permissions = Witchcraft.get().source("com.intrbiz.data.bergamot").getRegistry().timer(Witchcraft.name(BergamotDB.class, "bergamot.build_permissions(UUID)"));
    
    /**
     * Compute the permissions model for the given site
     * @param siteId
     * @return the number of permissions computed
     */
    public int buildPermissions(UUID siteId)
    {
        // compute a flattened view of the permissions
        int changed = this.useTimed(
            build_permissions,
            (with) -> {
                try (PreparedStatement stmt = with.prepareStatement("SELECT bergamot.build_permissions(?::UUID)"))
                {
                  stmt.setObject(1, siteId);
                  try (ResultSet rs = stmt.executeQuery())
                  {
                    if (rs.next()) return rs.getInt(1);
                  }
                }
                return null;
            }
        );
        // cache management
        this.invalidatePermissionsCache(siteId);
        // return the number of permissions which were computed
        return changed;
    }
    
    public void invalidatePermissionsCache(UUID siteId)
    {
        this.getAdapterCache().removePrefix("has_permission." + siteId);
        this.getAdapterCache().removePrefix("has_permission_for_object." + siteId);
        this.getAdapterCache().removePrefix("has_permission_for_domain." + siteId);
    }
    
    private Timer has_permission = Witchcraft.get().source("com.intrbiz.data.bergamot").getRegistry().timer(Witchcraft.name(BergamotDB.class, "bergamot.has_permission(UUID,TEXT)"));
    private Meter cache_miss_has_permission = Witchcraft.get().source("com.intrbiz.data.bergamot").getRegistry().meter(Witchcraft.name(BergamotDB.class, "cache_miss.bergamot.has_permission(UUID,TEXT)"));
    
    /**
     * Does the given contact have the given permission
     * @param contactId the contact
     * @param permission the permission
     * @return true if the contact has permission
     */
    public boolean hasPermission(UUID contactId, String permission)
    {
        return this.useTimedCached(
            has_permission, 
            cache_miss_has_permission, 
            "has_permission." + this.getSiteId(contactId) + "." + contactId + "." + permission, 
            null, 
            (with) -> {
                try (PreparedStatement stmt = with.prepareStatement("SELECT bergamot.has_permission(?::UUID, ?::TEXT)"))
                {
                  stmt.setObject(1, contactId);
                  stmt.setString(2, permission);
                  try (ResultSet rs = stmt.executeQuery())
                  {
                    if (rs.next()) return rs.getBoolean(1);
                  }
                }
                return false;
            }
        );
    }
    
    private Timer has_permission_for_domain = Witchcraft.get().source("com.intrbiz.data.bergamot").getRegistry().timer(Witchcraft.name(BergamotDB.class, "bergamot.has_permission_for_domain(UUID,UUID,TEXT)"));
    private Meter cache_miss_has_permission_for_domain = Witchcraft.get().source("com.intrbiz.data.bergamot").getRegistry().meter(Witchcraft.name(BergamotDB.class, "cache_miss.bergamot.has_permission_for_domain(UUID,UUID,TEXT)"));
    
    /**
     * Does the given contact have the given permission for the given security domain
     * @param contactId the contact
     * @param securityDomainId the security domain
     * @param permission the permission
     * @return true if the contact has permission
     */
    public boolean hasPermissionForSecurityDomain(UUID contactId, UUID securityDomainId, String permission)
    {
        return this.useTimedCached(
            has_permission_for_domain, 
            cache_miss_has_permission_for_domain, 
            "has_permission_for_domain." + this.getSiteId(contactId) + "." + contactId + "." + securityDomainId + "." + permission,
            null, 
            (with) -> {
                try (PreparedStatement stmt = with.prepareStatement("SELECT bergamot.has_permission_for_domain(?::UUID, ?::UUID, ?::TEXT)"))
                {
                  stmt.setObject(1, contactId);
                  stmt.setObject(2, securityDomainId);
                  stmt.setString(3, permission);
                  try (ResultSet rs = stmt.executeQuery())
                  {
                    if (rs.next()) return rs.getBoolean(1);
                  }
                }
                return false;
            }
        );
    }
    
    private Timer has_permission_for_object = Witchcraft.get().source("com.intrbiz.data.bergamot").getRegistry().timer(Witchcraft.name(BergamotDB.class, "bergamot.has_permission_for_object(UUID,UUID,TEXT)"));
    private Meter cache_miss_has_permission_for_object = Witchcraft.get().source("com.intrbiz.data.bergamot").getRegistry().meter(Witchcraft.name(BergamotDB.class, "cache_miss.bergamot.has_permission_for_object(UUID,UUID,TEXT)"));
    
    /**
     * Does the given contact have the given permission for the given object
     * @param contactId the contact
     * @param objectId the object
     * @param permission the permission
     * @return true if the contact has permission
     */
    public boolean hasPermissionForObject(UUID contactId, UUID objectId, String permission)
    {
        return this.useTimedCached(
            has_permission_for_object, 
            cache_miss_has_permission_for_object, 
            "has_permission_for_object." + this.getSiteId(contactId) + "." + contactId + "." + objectId + "." + permission, 
            null, 
            (with) -> {
                try (PreparedStatement stmt = with.prepareStatement("SELECT bergamot.has_permission_for_object(?::UUID, ?::UUID, ?::TEXT)"))
                {
                  stmt.setObject(1, contactId);
                  stmt.setObject(2, objectId);
                  stmt.setString(3, permission);
                  try (ResultSet rs = stmt.executeQuery())
                  {
                    if (rs.next()) return rs.getBoolean(1);
                  }
                }
                return false;
            }
        );
    }
    
    // helpers
    
    /**
     * Get the site id from a given object id
     */
    public UUID getSiteId(UUID objectId)
    {
        return Site.getSiteId(objectId);
    }
    
    // patches
    
    @SQLPatch(name = "move_stats_from_state", index = 1, type = ScriptType.UPGRADE, version = @SQLVersion({1, 2, 0}), skip = false)
    public static SQLScript moveStatsFromState()
    {
        return new SQLScript(
            "INSERT INTO bergamot.check_stats (check_id, last_runtime, average_runtime, last_check_execution_latency, average_check_execution_latency, last_check_processing_latency, average_check_processing_latency) (SELECT check_id, last_runtime, average_runtime, last_check_execution_latency, average_check_execution_latency, last_check_processing_latency, average_check_processing_latency FROM bergamot.check_state)"
        );
    }
    
    @SQLPatch(name = "drop_state_stats_columns", index = 2, type = ScriptType.UPGRADE, version = @SQLVersion({1, 2, 0}), skip = false)
    public static SQLScript dropStateStatsColumns()
    {
        return new SQLScript(
          // state table
          "ALTER TABLE bergamot.check_state DROP COLUMN last_runtime",
          "ALTER TABLE bergamot.check_state DROP COLUMN average_runtime",
          "ALTER TABLE bergamot.check_state DROP COLUMN last_check_execution_latency",
          "ALTER TABLE bergamot.check_state DROP COLUMN average_check_execution_latency",
          "ALTER TABLE bergamot.check_state DROP COLUMN last_check_processing_latency",
          "ALTER TABLE bergamot.check_state DROP COLUMN average_check_processing_latency",
          // state type
          "ALTER TYPE bergamot.t_check_state DROP ATTRIBUTE last_runtime",
          "ALTER TYPE bergamot.t_check_state DROP ATTRIBUTE average_runtime",
          "ALTER TYPE bergamot.t_check_state DROP ATTRIBUTE last_check_execution_latency",
          "ALTER TYPE bergamot.t_check_state DROP ATTRIBUTE average_check_execution_latency",
          "ALTER TYPE bergamot.t_check_state DROP ATTRIBUTE last_check_processing_latency",
          "ALTER TYPE bergamot.t_check_state DROP ATTRIBUTE average_check_processing_latency"
        );
    }
    
    @SQLPatch(name = "add_check_transition_indexes", index = 3, type = ScriptType.BOTH, version = @SQLVersion({1, 3, 3}), skip = false)
    public static SQLScript upgradeCheckTransitionIndexes()
    {
        return new SQLScript(
           "CREATE INDEX check_transition_check_id_applied_at_idx ON bergamot.check_transition (check_id, applied_at)"
        );
    }
    
    @SQLPatch(name = "add_validate_group_ids", index = 1, type = ScriptType.BOTH, version = @SQLVersion({1, 6, 0}), skip = false)
    public static SQLScript addValidateGroupIds()
    {
        return new SQLScript(
              "CREATE OR REPLACE FUNCTION bergamot.validate_group_ids(p_ids uuid[])\n" +
              "RETURNS boolean AS\n" +
              "$BODY$\n" +
              "DECLARE\n" +
              "  v_ret boolean;\n" +
              "BEGIN\n" +
              "  v_ret := true;\n" +
              "  IF p_ids IS NOT NULL THEN\n" +
              "    SELECT (count(*) = array_length(p_ids, 1)) INTO v_ret FROM bergamot.group WHERE p_ids @> ARRAY[id];\n" +
              "  END IF;\n" +
              "  RETURN v_ret;\n" +
              "END;\n" +
              "$BODY$\n" +
              "LANGUAGE plpgsql VOLATILE",
              "ALTER FUNCTION bergamot.validate_group_ids(uuid[]) OWNER TO bergamot"
        );
    }
    
    @SQLPatch(name = "add_validate_team_ids", index = 2, type = ScriptType.BOTH, version = @SQLVersion({1, 6, 0}), skip = false)
    public static SQLScript addValidateTeamIds()
    {
        return new SQLScript(
              "CREATE OR REPLACE FUNCTION bergamot.validate_team_ids(p_ids uuid[])\n" +
              "RETURNS boolean AS\n" +
              "$BODY$\n" +
              "DECLARE\n" +
              "  v_ret boolean;\n" +
              "BEGIN\n" +
              "  v_ret := true;\n" +
              "  IF p_ids IS NOT NULL THEN\n" +
              "    SELECT (count(*) = array_length(p_ids, 1)) INTO v_ret FROM bergamot.team WHERE p_ids @> ARRAY[id];\n" +
              "  END IF;\n" +
              "  RETURN v_ret;\n" +
              "END;\n" +
              "$BODY$\n" +
              "LANGUAGE plpgsql VOLATILE",
              "ALTER FUNCTION bergamot.validate_team_ids(uuid[]) OWNER TO bergamot"
        );
    }
    
    @SQLPatch(name = "add_validate_contact_ids", index = 3, type = ScriptType.BOTH, version = @SQLVersion({1, 6, 0}), skip = false)
    public static SQLScript addValidateContactIds()
    {
        return new SQLScript(
              "CREATE OR REPLACE FUNCTION bergamot.validate_contact_ids(p_ids uuid[])\n" +
              "RETURNS boolean AS\n" +
              "$BODY$\n" +
              "DECLARE\n" +
              "  v_ret boolean;\n" +
              "BEGIN\n" +
              "  v_ret := true;\n" +
              "  IF p_ids IS NOT NULL THEN\n" +
              "    SELECT (count(*) = array_length(p_ids, 1)) INTO v_ret FROM bergamot.contact WHERE p_ids @> ARRAY[id];\n" +
              "  END IF;\n" +
              "  RETURN v_ret;\n" +
              "END;\n" +
              "$BODY$\n" +
              "LANGUAGE plpgsql VOLATILE",
              "ALTER FUNCTION bergamot.validate_contact_ids(uuid[]) OWNER TO bergamot"
        );
    }
    
    @SQLPatch(name = "add_site_alias_index", index = 5, type = ScriptType.BOTH, version = @SQLVersion({1, 6, 0}), skip = false)
    public static SQLScript addSiteAliasIndex()
    {
        return new SQLScript(
                "CREATE INDEX \"site_aliases_idx\" ON bergamot.site USING gin (aliases)"
        );
    }
    
    @SQLPatch(name = "add_info_and_action_statuses", index = 6, type = ScriptType.UPGRADE, version = @SQLVersion({2, 4, 0}), skip = false)
    public static SQLScript addInfoAndActionStatuses()
    {
        return new SQLScript(
                // check state
                "UPDATE bergamot.check_state " +
                "SET " +
                " status=(CASE WHEN status = 0 THEN 0 ELSE status + 1 END), " +
                " last_hard_status=(CASE WHEN last_hard_status = 0 THEN 0 ELSE last_hard_status + 1 END) ",
                // check transitions
                "UPDATE bergamot.check_transition " +
                "SET " +
                " previous_status=(CASE WHEN previous_status = 0 THEN 0 ELSE previous_status + 1 END), " +
                " previous_last_hard_status=(CASE WHEN previous_last_hard_status = 0 THEN 0 ELSE previous_last_hard_status + 1 END), " +
                " next_status=(CASE WHEN next_status = 0 THEN 0 ELSE next_status + 1 END), " +
                " next_last_hard_status=(CASE WHEN next_last_hard_status = 0 THEN 0 ELSE next_last_hard_status + 1 END)",
                // alerts
                "UPDATE bergamot.alert "+
                "SET " +
                " status=(CASE WHEN status = 0 THEN 0 ELSE status + 1 END), " +
                " last_hard_status=(CASE WHEN last_hard_status = 0 THEN 0 ELSE last_hard_status + 1 END) "
        );
    }
    
    @SQLPatch(name = "add_downtime_indexes", index = 7, type = ScriptType.BOTH, version = @SQLVersion({2, 9, 0}), skip = false)
    public static SQLScript addDowntimeIndexes()
    {
        return new SQLScript(
                "CREATE INDEX \"downtime_check_id_idx\" ON bergamot.downtime USING btree (check_id)",
                "CREATE INDEX \"downtime_starts_ends_idx\" ON bergamot.downtime USING btree (starts, ends)"
        );
    }
    
    @SQLPatch(name = "add_acknowledge_notifications", index = 8, type = ScriptType.UPGRADE, version = @SQLVersion({3, 2, 0}), skip = false)
    public static SQLScript addAcknowledgeNotifications()
    {
        return new SQLScript(
                "UPDATE bergamot.notifications SET acknowledge_enabled = TRUE",
                "UPDATE bergamot.notification_engine SET acknowledge_enabled = TRUE"
        );
    }

    @SQLPatch(name = "add_downtime_state", index = 9, type = ScriptType.UPGRADE, version = @SQLVersion({3, 3, 0}), skip = false)
    public static SQLScript addDowntimeState()
    {
        return new SQLScript(
                "UPDATE bergamot.check_state SET in_downtime = FALSE",
                "UPDATE bergamot.check_transition SET previous_in_downtime = FALSE, next_in_downtime = FALSE"
        );
    }
    
    @SQLPatch(name = "add_suppressed_state", index = 9, type = ScriptType.UPGRADE, version = @SQLVersion({3, 4, 0}), skip = false)
    public static SQLScript addSuppressedState()
    {
        return new SQLScript(
                "UPDATE bergamot.check_state SET suppressed = FALSE",
                "UPDATE bergamot.check_transition SET previous_suppressed = FALSE, next_suppressed = FALSE"
        );
    }
    
    // V2 ACLs
    
    @SQLPatch(name = "add_acl_processing", index = 10, type = ScriptType.BOTH, version = @SQLVersion({3, 11, 0}), skip = false)
    public static SQLScript addACLProcessing()
    {
        return new SQLScript(
                "CREATE OR REPLACE FUNCTION bergamot.match_permission(p_grants TEXT[], p_permission TEXT) \n" +
                "RETURNS BOOLEAN \n" +
                "LANGUAGE SQL \n" +
                "AS $$ \n" +
                " SELECT bool_or(q.v) \n" +
                " FROM \n" +
                " ( \n" +
                "   SELECT \n" + 
                "     CASE \n" +
                "      WHEN g.v ~ '\\*$' THEN \n" +
                "       substring(g.v, 1, length(g.v) - 1) = substring($2, 1, length(g.v) - 1) \n" +
                "      ELSE \n" +
                "       g.v = $2 \n" +
                "     END \n" +
                "   FROM unnest($1) g(v) \n" +
                "  UNION \n" +
                "   SELECT FALSE \n" +
                " ) q(v) \n" +
                "$$",
                
                "CREATE OR REPLACE FUNCTION bergamot.check_permission(p_contact_id UUID, p_permission TEXT)\n" +
                "RETURNS BOOLEAN\n" +
                "LANGUAGE SQL\n" +
                "AS $$\n" +
                " SELECT (bool_or(granted_generic) AND NOT bool_or(revoked_generic)) AS allowed\n" +
                " FROM\n" +
                " (\n" +
                "  WITH RECURSIVE team_graph(id, team_ids, granted_permissions, revoked_permissions, depth, chain) AS (\n" +
                "    SELECT t1.id, t1.team_ids, t1.granted_permissions, t1.revoked_permissions, 1, ARRAY[t1.id]\n" +
                "    FROM bergamot.team t1\n" +
                "   UNION\n" +
                "    SELECT t2.id, t2.team_ids, t2.granted_permissions, t2.revoked_permissions, tg.depth + 1, tg.chain || t2.id\n" +
                "    FROM bergamot.team t2, team_graph tg\n" +
                "    WHERE tg.team_ids @> ARRAY[t2.id]\n" +
                "  )\n" +
                "  SELECT \n" +
                "   c.id, \n" +
                "   0 AS depth, \n" +
                "   ARRAY[c.id] AS chain, \n" +
                "   bergamot.match_permission(c.granted_permissions,  $2) AS granted_generic, \n" +
                "   bergamot.match_permission(c.revoked_permissions,  $2) AS revoked_generic \n" +
                "  FROM bergamot.contact c\n" +
                "  WHERE c.id = $1\n" +
                " UNION ALL\n" +
                "  SELECT \n" +
                "   tg.id, \n" +
                "   tg.depth, \n" +
                "   tg.chain, \n" +
                "   bergamot.match_permission(tg.granted_permissions, $2) AS granted_generic, \n" +
                "   bergamot.match_permission(tg.revoked_permissions, $2) AS revoked_generic\n" +
                "  FROM bergamot.contact c\n" +
                "  JOIN team_graph tg ON (c.team_ids && tg.chain)\n" +
                "  WHERE c.id = $1\n" +
                " ) q\n" +
                "$$",
                
                "CREATE OR REPLACE FUNCTION bergamot.check_permission_for_domain(p_contact_id UUID, p_security_domain_id UUID, p_permission TEXT)\n" +
                "RETURNS BOOLEAN\n" +
                "LANGUAGE SQL\n" +
                "AS $$\n" +
                " SELECT ((bool_or(granted_generic) AND NOT bool_or(revoked_generic)) OR (bool_or(granted_domain) AND NOT bool_or(revoked_domain))) AS allowed\n" +
                " FROM\n" +
                " (\n" +
                "  WITH RECURSIVE team_graph(id, team_ids, granted_permissions, revoked_permissions, depth, chain) AS (\n" +
                "    SELECT t1.id, t1.team_ids, t1.granted_permissions, t1.revoked_permissions, 1, ARRAY[t1.id]\n" +
                "    FROM bergamot.team t1\n" +
                "   UNION\n" +
                "    SELECT t2.id, t2.team_ids, t2.granted_permissions, t2.revoked_permissions, tg.depth + 1, tg.chain || t2.id\n" +
                "    FROM bergamot.team t2, team_graph tg\n" +
                "    WHERE tg.team_ids @> ARRAY[t2.id]\n" +
                "  )\n" +
                "  SELECT \n" +
                "   c.id, \n" +
                "   0 AS depth, \n" +
                "   ARRAY[c.id] AS chain, \n" +
                "   bergamot.match_permission(c.granted_permissions,  $3) AS granted_generic, \n" +
                "   bergamot.match_permission(c.revoked_permissions,  $3) AS revoked_generic, \n" +
                "   bergamot.match_permission(ac.granted_permissions, $3) AS granted_domain, \n" +
                "   bergamot.match_permission(ac.revoked_permissions, $3) AS revoked_domain\n" +
                "  FROM bergamot.contact c\n" +
                "  LEFT JOIN bergamot.access_control ac ON (c.id = ac.role_id AND ac.security_domain_id = $2)\n" +
                "  WHERE c.id = $1\n" +
                " UNION ALL\n" +
                "  SELECT \n" +
                "   tg.id, \n" +
                "   tg.depth, \n" +
                "   tg.chain, \n" +
                "   bergamot.match_permission(tg.granted_permissions, $3) AS granted_generic, \n" +
                "   bergamot.match_permission(tg.revoked_permissions, $3) AS revoked_generic, \n" +
                "   bergamot.match_permission(ac.granted_permissions, $3) AS granted_domain, \n" +
                "   bergamot.match_permission(ac.revoked_permissions, $3) AS revoked_domain\n" +
                "  FROM bergamot.contact c\n" +
                "  JOIN team_graph tg ON (c.team_ids && tg.chain)\n" +
                "  LEFT JOIN bergamot.access_control ac ON (tg.id = ac.role_id AND ac.security_domain_id = $2)\n" +
                "  WHERE c.id = $1\n" +
                " ) q\n" +
                "$$",
                
                "CREATE OR REPLACE FUNCTION bergamot.list_permissions()\n" +
                "RETURNS SETOF TEXT\n" +
                "LANGUAGE SQL AS $$\n" +
                "  SELECT unnest(\n" +
                "      ARRAY[\n" +
                "        'ui.access',\n" +
                "        'ui.view.stats',\n" +
                "        'ui.view.stats.transitions',\n" +
                "        'ui.view.readings',\n" +
                "        'ui.sign.agent',\n" +
                "        'ui.generate.agent',\n" +
                "        'ui.admin',\n" +
                "        'api.access',\n" +
                "        'api.sign.agent',\n" +
                "        'read',\n" +
                "        'read.config',\n" +
                "        'read.comment',\n" +
                "        'read.downtime',\n" +
                "        'read.readings',\n" +
                "        'enable',\n" +
                "        'disable',\n" +
                "        'execute',\n" +
                "        'suppress',\n" +
                "        'unsuppress',\n" +
                "        'submit',\n" +
                "        'acknowledge',\n" +
                "        'write',\n" +
                "        'write.comment',\n" +
                "        'write.downtime',\n" +
                "        'create',\n" +
                "        'remove',\n" +
                "        'remove.comment',\n" +
                "        'remove.downtime',\n" +
                "        'sign.agent',\n" +
                "        'config.export',\n" +
                "        'config.change.apply'\n" +
                "      ])::TEXT;\n" +
                "$$",

                "CREATE OR REPLACE FUNCTION bergamot.compute_permissions(p_contact_id UUID)\n" +
                "RETURNS INTEGER \n" +
                "LANGUAGE plpgsql VOLATILE\n" +
                "AS $$\n" +
                "DECLARE\n" +
                "  v_count INTEGER;\n" +
                "BEGIN\n" +
                "    -- remove first\n" +
                "    DELETE FROM bergamot.computed_permissions WHERE contact_id = p_contact_id;\n" +
                "    -- now load\n" +
                "    INSERT INTO bergamot.computed_permissions \n" +
                "    (\n" +
                "      SELECT p_contact_id, p.permission, bergamot.check_permission(p_contact_id, p.permission) AS allowed\n" +
                "      FROM bergamot.list_permissions() p(permission)\n" +
                "    );\n" +
                "    GET DIAGNOSTICS v_count = ROW_COUNT;\n" +
                "    RETURN v_count;\n" +
                "END;\n" +
                "$$",

                "CREATE OR REPLACE FUNCTION bergamot.compute_permissions_for_domain(p_contact_id UUID, p_security_domain_id UUID)\n" +
                "RETURNS INTEGER \n" +
                "LANGUAGE plpgsql VOLATILE\n" +
                "AS $$\n" +
                "DECLARE\n" +
                "  v_count INTEGER;\n" +
                "BEGIN\n" +
                "    -- remove first\n" +
                "    DELETE FROM bergamot.computed_permissions_for_domain WHERE contact_id = p_contact_id AND security_domain_id = p_security_domain_id;\n" +
                "    -- now load\n" +
                "    INSERT INTO bergamot.computed_permissions_for_domain \n" +
                "    (\n" +
                "      SELECT p_contact_id, p_security_domain_id, p.permission, bergamot.check_permission_for_domain(p_contact_id, p_security_domain_id, p.permission) AS allowed\n" +
                "      FROM bergamot.list_permissions() p(permission)\n" +
                "    );\n" +
                "    GET DIAGNOSTICS v_count = ROW_COUNT;\n" +
                "    RETURN v_count;\n" +
                "END;\n" +
                "$$",

                "CREATE OR REPLACE FUNCTION bergamot.build_permissions(p_site_id UUID)\n" +
                "RETURNS INTEGER \n" +
                "LANGUAGE PLPGSQL VOLATILE AS\n" +
                "$$\n" +
                "BEGIN\n" +
                "    -- compute the permissions\n" +
                "    RETURN sum(q.cp)\n" +
                "    FROM\n" +
                "    (\n" +
                "        SELECT bergamot.compute_permissions(c.id) AS cp \n" +
                "        FROM bergamot.contact c \n" + 
                "        WHERE c.site_id = p_site_id\n" +
                "      UNION ALL\n" +
                "        SELECT bergamot.compute_permissions_for_domain(c.id, sd.id) AS cp \n" +
                "        FROM bergamot.contact c " +
                "        CROSS JOIN bergamot.security_domain sd \n" +
                "        WHERE c.site_id = p_site_id\n" +
                "    ) q;\n" +
                "END;\n" +
                "$$",
                
                "CREATE OR REPLACE FUNCTION bergamot.has_permission(p_contact_id UUID, p_permission TEXT)\n" +
                "RETURNS BOOLEAN \n" +
                "LANGUAGE SQL STABLE AS\n" +
                "$$\n" +
                "    SELECT allowed \n" +
                "    FROM bergamot.computed_permissions \n" +
                "    WHERE contact_id = $1 AND permission = $2\n" +
                "$$",

                "CREATE OR REPLACE FUNCTION bergamot.has_permission_for_domain(p_contact_id UUID, p_security_domain_id UUID, p_permission TEXT)\n" +
                "RETURNS BOOLEAN \n" +
                "LANGUAGE SQL STABLE AS\n" +
                "$$\n" +
                "    SELECT cpfd.allowed \n" +
                "    FROM bergamot.computed_permissions_for_domain cpfd\n" +
                "    WHERE cpfd.contact_id = $1 AND cpfd.security_domain_id = $2 AND permission = $3\n" +
                "$$",

                "CREATE OR REPLACE FUNCTION bergamot.has_permission_for_object(p_contact_id UUID, p_object_id UUID, p_permission TEXT)\n" +
                "RETURNS BOOLEAN \n" +
                "LANGUAGE SQL STABLE AS\n" +
                "$$\n" +
                "  SELECT coalesce(bool_or(q.allowed), false) AS allowed\n" +
                "  FROM\n" +
                "  (\n" +
                "      SELECT cpfd.allowed\n" + 
                "      FROM bergamot.computed_permissions_for_domain cpfd\n" + 
                "      JOIN bergamot.security_domain_membership sdm ON (cpfd.security_domain_id = sdm.security_domain_id)\n" +
                "      WHERE cpfd.contact_id = $1 AND sdm.check_id = $2 AND cpfd.permission = $3\n" +
                "    UNION ALL\n" +
                "      SELECT allowed\n" + 
                "      FROM bergamot.computed_permissions\n" + 
                "      WHERE contact_id = $1 AND permission = $3\n" +
                "  ) q\n" +
                "$$"
        );
    }
    
    @SQLPatch(name = "add_ui_create_permission", index = 11, type = ScriptType.BOTH, version = @SQLVersion({3, 18, 0}), skip = false)
    public static SQLScript addUiCreatePermission()
    {
        return new SQLScript(
                
                "CREATE OR REPLACE FUNCTION bergamot.list_permissions()\n" +
                "RETURNS SETOF TEXT\n" +
                "LANGUAGE SQL AS $$\n" +
                "  SELECT unnest(\n" +
                "      ARRAY[\n" +
                "        'ui.access',\n" +
                "        'ui.view.stats',\n" +
                "        'ui.view.stats.transitions',\n" +
                "        'ui.view.readings',\n" +
                "        'ui.sign.agent',\n" +
                "        'ui.generate.agent',\n" +
                "        'ui.admin',\n" +
                "        'ui.create',\n" +
                "        'api.access',\n" +
                "        'api.sign.agent',\n" +
                "        'read',\n" +
                "        'read.config',\n" +
                "        'read.comment',\n" +
                "        'read.downtime',\n" +
                "        'read.readings',\n" +
                "        'enable',\n" +
                "        'disable',\n" +
                "        'execute',\n" +
                "        'suppress',\n" +
                "        'unsuppress',\n" +
                "        'submit',\n" +
                "        'acknowledge',\n" +
                "        'write',\n" +
                "        'write.comment',\n" +
                "        'write.downtime',\n" +
                "        'create',\n" +
                "        'remove',\n" +
                "        'remove.comment',\n" +
                "        'remove.downtime',\n" +
                "        'sign.agent',\n" +
                "        'config.export',\n" +
                "        'config.change.apply'\n" +
                "      ])::TEXT;\n" +
                "$$",
                
                "SELECT bergamot.build_permissions(id) FROM bergamot.site"
        );
    }
    
    public static void main(String[] args) throws Exception
    {
        if (args.length == 1 && "install".equals(args[0]))
        {
            DatabaseAdapterCompiler.main(new String[] { "install", BergamotDB.class.getCanonicalName() });
        }
        else if (args.length == 2 && "upgrade".equals(args[0]))
        {
            DatabaseAdapterCompiler.main(new String[] { "upgrade", BergamotDB.class.getCanonicalName(), args[1] });
        }
        else
        {
            // interactive
            try (Scanner input = new Scanner(System.in))
            {
                for (;;)
                {
                    System.out.print("Would you like to generate the install or upgrade schema: ");
                    String action = input.nextLine();
                    // process the action
                    if ("exit".equals(action) || "quit".equals(action) || "q".equals(action))
                    {
                        System.exit(0);
                    }
                    else if ("install".equalsIgnoreCase(action) || "in".equalsIgnoreCase(action) || "i".equalsIgnoreCase(action))
                    {
                        DatabaseAdapterCompiler.main(new String[] { "install", BergamotDB.class.getCanonicalName() });
                        System.exit(0);
                    }
                    else if ("upgrade".equalsIgnoreCase(action) || "up".equalsIgnoreCase(action) || "u".equalsIgnoreCase(action))
                    {
                        System.out.print("What is the current installed version: ");
                        String version = input.nextLine();
                        DatabaseAdapterCompiler.main(new String[] { "upgrade", BergamotDB.class.getCanonicalName(), version });
                        System.exit(0);
                    }
                }
            }
        }
    }
}
