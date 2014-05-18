package com.intrbiz.bergamot.store;

import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.Util;
import com.intrbiz.bergamot.config.model.ActiveCheckCfg;
import com.intrbiz.bergamot.config.model.BergamotCfg;
import com.intrbiz.bergamot.config.model.CheckCfg;
import com.intrbiz.bergamot.config.model.ClusterCfg;
import com.intrbiz.bergamot.config.model.ContactCfg;
import com.intrbiz.bergamot.config.model.GroupCfg;
import com.intrbiz.bergamot.config.model.HostCfg;
import com.intrbiz.bergamot.config.model.LocationCfg;
import com.intrbiz.bergamot.config.model.NotificationEngineCfg;
import com.intrbiz.bergamot.config.model.NotificationsCfg;
import com.intrbiz.bergamot.config.model.PassiveCheckCfg;
import com.intrbiz.bergamot.config.model.ResourceCfg;
import com.intrbiz.bergamot.config.model.ServiceCfg;
import com.intrbiz.bergamot.config.model.TeamCfg;
import com.intrbiz.bergamot.config.model.TimePeriodCfg;
import com.intrbiz.bergamot.config.model.TrapCfg;
import com.intrbiz.bergamot.config.model.VirtualCheckCfg;
import com.intrbiz.bergamot.model.ActiveCheck;
import com.intrbiz.bergamot.model.Check;
import com.intrbiz.bergamot.model.Cluster;
import com.intrbiz.bergamot.model.Command;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.Group;
import com.intrbiz.bergamot.model.Host;
import com.intrbiz.bergamot.model.Location;
import com.intrbiz.bergamot.model.NotificationEngine;
import com.intrbiz.bergamot.model.Notifications;
import com.intrbiz.bergamot.model.PassiveCheck;
import com.intrbiz.bergamot.model.Resource;
import com.intrbiz.bergamot.model.Service;
import com.intrbiz.bergamot.model.Status;
import com.intrbiz.bergamot.model.Team;
import com.intrbiz.bergamot.model.TimePeriod;
import com.intrbiz.bergamot.model.Trap;
import com.intrbiz.bergamot.model.VirtualCheck;
import com.intrbiz.bergamot.model.virtual.VirtualCheckOperator;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParser;
import com.intrbiz.bergamot.virtual.VirtualCheckExpressionParserContext;

public class BergamotConfigLoader
{
    private final Logger logger = Logger.getLogger(BergamotConfigLoader.class);

    private final ObjectStore store;

    private final BergamotCfg config;

    public BergamotConfigLoader(ObjectStore store, BergamotCfg config)
    {
        this.store = store;
        this.config = config;
    }

    public void load()
    {
        this.loadLocations();
        this.loadGroups();
        this.loadTeams();
        this.loadTimePeriods();
        this.loadContacts();
        // load the real things
        this.loadHosts();
        this.loadClusters();
    }

    private void loadLocations()
    {
        for (LocationCfg cfg : this.config.getLocations())
        {
            if (!cfg.getTemplateBooleanValue())
            {
                logger.info("Loading location " + cfg.resolve().getName());
                // load
                Location l = new Location();
                l.configure(cfg);
                this.store.addLocation(l);
            }
        }
        // link the tree
        for (Location l : this.store.getLocations())
        {
            String pn = l.getConfiguration().resolve().getLocation();
            if (!Util.isEmpty(pn))
            {
                Location p = this.store.lookupLocation(pn);
                if (p != null)
                {
                    logger.info("Adding location " + l.getName() + " to location " + p.getName());
                    p.addLocation(l);
                }
            }
        }
    }

    private void loadGroups()
    {
        for (GroupCfg cfg : this.config.getGroups())
        {
            if (!cfg.getTemplateBooleanValue())
            {
                logger.info("Loading group " + cfg.resolve().getName());
                // load
                Group g = new Group();
                g.configure(cfg);
                this.store.addGroup(g);
            }
        }
        // link the tree
        for (Group g : this.store.getGroups())
        {
            for (String pn : g.getConfiguration().resolve().getGroups())
            {
                Group p = this.store.lookupGroup(pn);
                if (p != null)
                {
                    logger.info("Adding group " + g.getName() + " to group " + p.getName());
                    p.addChild(g);
                }
            }
        }
    }

    private void loadTimePeriods()
    {
        for (TimePeriodCfg cfg : this.config.getTimePeriods())
        {
            if (!cfg.getTemplateBooleanValue())
            {
                logger.info("Loading time period " + cfg.resolve().getName());
                // load
                TimePeriod t = new TimePeriod();
                t.configure(cfg);
                this.store.addTimePeriod(t);
            }
        }
        // link excludes
        for (TimePeriod t : this.store.getTimePeriods())
        {
            for (String en : t.getConfiguration().resolve().getExcludes())
            {
                TimePeriod e = this.store.lookupTimePeriod(en);
                if (e != null)
                {
                    logger.info("Adding exclude time period " + e.getName() + " to time period " + t.getName());
                    t.addExclude(e);
                }
            }
        }
    }

    private void loadTeams()
    {
        for (TeamCfg cfg : this.config.getTeams())
        {
            if (!cfg.getTemplateBooleanValue())
            {
                logger.info("Loading team " + cfg.resolve().getName());
                // load
                Team t = new Team();
                t.configure(cfg);
                this.store.addTeam(t);
            }
        }
        // link the tree
        for (Team t : this.store.getTeams())
        {
            for (String pn : t.getConfiguration().resolve().getTeams())
            {
                Team p = this.store.lookupTeam(pn);
                if (p != null)
                {
                    logger.info("Adding team " + t.getName() + " to team " + p.getName());
                    p.addChild(t);
                }
            }
        }
    }

    private void loadContacts()
    {
        for (ContactCfg cfg : this.config.getContacts())
        {
            ContactCfg rcfg = cfg.resolve();
            if (!cfg.getTemplateBooleanValue())
            {
                logger.info("Loading contact " + cfg.resolve().getName());
                // load
                Contact c = new Contact();
                c.configure(cfg);
                // notifications
                c.setNotifications(this.loadNotifications(rcfg.getNotifications()));
                // store
                this.store.addContact(c);
                // teams
                for (String tn : rcfg.getTeams())
                {
                    Team t = this.store.lookupTeam(tn);
                    if (t != null)
                    {
                        logger.info("Adding contact " + c.getName() + " to team " + t.getName());
                        t.addContact(c);
                    }
                }
            }
        }
    }

    private void loadHosts()
    {
        for (HostCfg cfg : this.config.getHosts())
        {
            if (!cfg.getTemplateBooleanValue())
            {
                // resolved config
                HostCfg rcfg = cfg.resolve();
                logger.info("Loading host " + rcfg.getName());
                // load
                Host h = new Host();
                h.configure(cfg);
                // load the check details
                this.loadActiveCheck(h, rcfg);
                // add locations
                String ln = rcfg.getLocation();
                if (!Util.isEmpty(ln))
                {
                    Location l = this.store.lookupLocation(ln);
                    if (l != null)
                    {
                        l.addHost(h);
                    }
                }
                // add the host
                this.store.addHost(h);
                // add services
                for (ServiceCfg scfg : rcfg.getServices())
                {
                    this.loadService(h, scfg);
                }
                // add traps
                for (TrapCfg tcfg : rcfg.getTraps())
                {
                    this.loadTrap(h, tcfg);
                }
            }
        }
    }

    private void loadService(Host host, ServiceCfg cfg)
    {
        // resolve
        ServiceCfg rcfg = cfg.resolve();
        logger.info("Adding service " + rcfg.getName() + " to host " + host.getName());
        // create the service
        Service s = new Service();
        s.configure(cfg);
        // load the check details
        this.loadActiveCheck(s, rcfg);
        // add
        this.store.addService(s);
        host.addService(s);
    }
    
    private void loadTrap(Host host, TrapCfg cfg)
    {
        // resolve
        TrapCfg rcfg = cfg.resolve();
        logger.info("Adding trap " + rcfg.getName() + " to host " + host.getName());
        // create the service
        Trap t = new Trap();
        t.configure(cfg);
        // load the check details
        this.loadPasiveCheck(t, rcfg);
        // add
        this.store.addTrap(t);
        host.addTrap(t);
    }
    
    private void loadPasiveCheck(PassiveCheck check, PassiveCheckCfg<?> rcfg)
    {
        this.loadCheck(check, rcfg);
    }

    private void loadActiveCheck(ActiveCheck check, ActiveCheckCfg<?> rcfg)
    {
        this.loadCheck(check, rcfg);
        // the check period
        String tpn = rcfg.getSchedule().getTimePeriod();
        if (!Util.isEmpty(tpn))
        {
            TimePeriod tp = this.store.lookupTimePeriod(tpn);
            if (tp != null)
            {
                check.setCheckPeriod(tp);
            }
        }
        // the check command
        if (rcfg.getCommand() != null)
        {
            Command command = new Command();
            command.configure(rcfg.getCommand());
            check.setCheckCommand(command);
            logger.info("Added command " + command.getName() + " to check " + check.getName());
        }
    }

    private void loadCheck(Check check, CheckCfg<?> rcfg)
    {
        // notifications
        check.setNotifications(this.loadNotifications(rcfg.getNotifications()));
        // contacts
        for (String tn : rcfg.getNotify().getTeams())
        {
            Team t = this.store.lookupTeam(tn);
            if (t != null)
            {
                check.addTeam(t);
            }
        }
        for (String cn : rcfg.getNotify().getContacts())
        {
            Contact c = this.store.lookupContact(cn);
            if (c != null)
            {
                check.addContact(c);
            }
        }
        // the groups
        for (String group : rcfg.getGroups())
        {
            Group g = this.store.lookupGroup(group);
            if (g != null)
            {
                logger.info("Adding check " + check.getName() + " to group " + g.getName());
                g.addCheck(check);
            }
        }
    }

    private Notifications loadNotifications(NotificationsCfg cfg)
    {
        Notifications n = new Notifications();
        n.setEnabled(cfg.getEnabledBooleanValue());
        n.setAlertsEnabled(cfg.getAlertsBooleanValue());
        n.setRecoveryEnabled(cfg.getRecoveryBooleanValue());
        n.setIgnore(cfg.getIgnore().stream().map((e) -> {
            return Status.valueOf(e.toUpperCase());
        }).collect(Collectors.toSet()));
        n.setAllEnginesEnabled(cfg.getAllEnginesEnabledBooleanValue());
        // load the time period
        if (! Util.isEmpty(cfg.getNotificationPeriod()))
        {
            TimePeriod tp = this.store.lookupTimePeriod(cfg.getNotificationPeriod());
            if (tp != null)
            {
                n.setTimePeriod(tp);
            }
        }
        // engines
        for (NotificationEngineCfg ecfg : cfg.getNotificationEngines())
        {
            NotificationEngine ne = new NotificationEngine();
            ne.setEnabled(ecfg.getEnabledBooleanValue());
            ne.setAlertsEnabled(ecfg.getAlertsBooleanValue());
            ne.setRecoveryEnabled(ecfg.getRecoveryBooleanValue());
            ne.setIgnore(ecfg.getIgnore().stream().map((e) -> {
                return Status.valueOf(e.toUpperCase());
            }).collect(Collectors.toSet()));
            ne.setEngine(ecfg.getEngine());
            if (! Util.isEmpty(ecfg.getNotificationPeriod()))
            {
                TimePeriod tp = this.store.lookupTimePeriod(ecfg.getNotificationPeriod());
                if (tp != null)
                {
                    ne.setTimePeriod(tp);
                }
            }
            n.addEngine(ne);
        }
        return n;
    }
    
    private void loadVirtualCheck(VirtualCheck check, VirtualCheckCfg<?> rcfg)
    {
        this.loadCheck(check, rcfg);
        // parse the condition
        if (! Util.isEmpty(rcfg.getCondition()))
        {
            VirtualCheckOperator cond = VirtualCheckExpressionParser.parseVirtualCheckExpression(this.createVirtualCheckContext(), rcfg.getCondition());
            if (cond != null)
            {
                check.setCondition(cond);
                logger.info("Using virtual check condition " + cond.toString() + " for " + check);
                // cross reference the checks
                check.setReferences(cond.computeDependencies());
                for (Check dep : check.getReferences())
                {
                    logger.info("The check " + dep + " is referenced by the virtual check " + check);
                    dep.addReferencedBy(check);
                }
            }
        }
    }
    
    private VirtualCheckExpressionParserContext createVirtualCheckContext()
    {
        return new VirtualCheckExpressionParserContext()
        {
            @Override
            public Host lookupHost(String name)
            {
                return store.lookupHost(name);
            }

            @Override
            public Host lookupHost(UUID id)
            {
                return store.lookupHost(id);
            }

            @Override
            public Cluster lookupCluster(String name)
            {
                return store.lookupCluster(name);
            }

            @Override
            public Cluster lookupCluster(UUID id)
            {
                return store.lookupCluster(id);
            }

            @Override
            public Service lookupService(Host on, String name)
            {
                return on.getService(name);
            }

            @Override
            public Service lookupService(UUID id)
            {
                return store.lookupService(id);
            }

            @Override
            public Trap lookupTrap(Host on, String name)
            {
                return on.getTrap(name);
            }

            @Override
            public Trap lookupTrap(UUID id)
            {
                return store.lookupTrap(id);
            }

            @Override
            public Resource lookupResource(Cluster on, String name)
            {
                return on.getResource(name);
            }

            @Override
            public Resource lookupResource(UUID id)
            {
                return store.lookupResource(id);
            }           
        };
    }
    
    private void loadClusters()
    {
        for (ClusterCfg cfg : this.config.getClusters())
        {
            if (!cfg.getTemplateBooleanValue())
            {
                // resolved config
                ClusterCfg rcfg = cfg.resolve();
                logger.info("Loading cluster " + rcfg.getName());
                // load
                Cluster c = new Cluster();
                c.configure(cfg);
                // load the check details
                this.loadVirtualCheck(c, rcfg);
                // add the cluster
                this.store.addCluster(c);
                // add resources
                for (ResourceCfg scfg : rcfg.getResources())
                {
                    this.loadResource(c, scfg);
                }
            }
        }
    }
    
    private void loadResource(Cluster cluster, ResourceCfg cfg)
    {
        // resolve
        ResourceCfg rcfg = cfg.resolve();
        logger.info("Adding resource " + rcfg.getName() + " to cluster " + cluster.getName());
        // create the service
        Resource r = new Resource();
        r.configure(cfg);
        // load the check details
        this.loadVirtualCheck(r, rcfg);
        // add
        this.store.addResource(r);
        cluster.addResource(r);
    }
}
