package com.intrbiz.bergamot.ui.router.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.intrbiz.balsa.engine.route.Router;
import com.intrbiz.balsa.metadata.WithDataAdapter;
import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.ProcessorConsumer;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.election.SchedulingPoolElector;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Contact;
import com.intrbiz.bergamot.model.GlobalSetting;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;
import com.intrbiz.bergamot.model.message.cluster.ProcessorRegistration;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;
import com.intrbiz.bergamot.ui.BergamotApp;
import com.intrbiz.metadata.Any;
import com.intrbiz.metadata.Before;
import com.intrbiz.metadata.CoalesceMode;
import com.intrbiz.metadata.CurrentPrincipal;
import com.intrbiz.metadata.IsaInt;
import com.intrbiz.metadata.IsaUUID;
import com.intrbiz.metadata.Param;
import com.intrbiz.metadata.Prefix;
import com.intrbiz.metadata.RequirePermission;
import com.intrbiz.metadata.RequireValidAccessTokenForURL;
import com.intrbiz.metadata.RequireValidPrincipal;
import com.intrbiz.metadata.Template;

@Prefix("/global/admin")
@Template("layout/main")
@RequireValidPrincipal()
@RequirePermission("ui.admin")
public class GlobalAdminRouter extends Router<BergamotApp>
{    
    private static final Logger logger = Logger.getLogger(GlobalAdminRouter.class);
    
    private static final String[] COLOURS = { "#a98cff", "#ff829d", "#6b9aff", "#dec1ff", "#ffa595", "#3973ac", "#bf5ad8", "#D9E1D9", "#00BF00", "#F3C300", "#E85752"  };
    
    @Before()
    @Any("**")
    @WithDataAdapter(BergamotDB.class)
    public void requireGlobalAdmin(BergamotDB db, @CurrentPrincipal Contact principal)
    {
        require(var("globalAdmin", principal.isGlobalAdmin()));
    }
    
    @Any("/")
    @WithDataAdapter(BergamotDB.class)
    public void index(BergamotDB db, @Param("level") @IsaInt(defaultValue = 0, coalesce = CoalesceMode.ALWAYS) Integer level) throws Exception
    {
        // workers, notifiers, processing pools and other cluster information
        // workers
        Set<WorkerRegistration> workers = app().getProcessor().getWorkerRegistry().getWorkers();
        var("workers", workers);
        var("worker_route_table", app().getProcessor().getWorkerRegistry().getRouteTable().toString());
        Map<UUID, Long> workerTails = new HashMap<UUID, Long>();
        Map<UUID, Long> workerCommited = new HashMap<UUID, Long>();
        for (WorkerRegistration worker : workers)
        {
            // TODO: we should have a task computing real stats somewhere
            WorkerConsumer consumer = new WorkerConsumer(app().getProcessor().getHazelcast(), worker.getId());
            workerTails.put(worker.getId(), consumer.getTailSequence());
            workerCommited.put(worker.getId(), consumer.getSequence());
        }
        var("worker_tail", workerTails);
        var("worker_commited", workerCommited);
        // notifiers
        Set<NotifierRegistration> notifiers = app().getProcessor().getNotifierRegistry().getNotifiers();
        var("notifiers", notifiers);
        var("notifier_route_table", app().getProcessor().getNotifierRegistry().getRouteTable().toString());
        Map<UUID, Long> notifierTails = new HashMap<UUID, Long>();
        Map<UUID, Long> notifierCommited = new HashMap<UUID, Long>();
        for (NotifierRegistration notifier : notifiers)
        {
            // TODO: we should have a task computing real stats somewhere
            NotificationConsumer consumer = new NotificationConsumer(app().getProcessor().getHazelcast(), notifier.getId());
            notifierTails.put(notifier.getId(), consumer.getTailSequence());
            notifierCommited.put(notifier.getId(), consumer.getSequence());
        }
        var("notifier_tail", notifierTails);
        var("notifier_commited", notifierCommited);
        // processors
        Map<UUID, ProcessorRegistration> processors = app().getProcessor().getProcessorRegistry().getProcessors().stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
        Map<UUID, String> colours = new HashMap<>();
        Map<UUID, Long> processorTails = new HashMap<UUID, Long>();
        Map<UUID, Long> processorCommited = new HashMap<UUID, Long>();
        int idx = 0;
        for (ProcessorRegistration processor : processors.values())
        {
            colours.put(processor.getId(), COLOURS[idx ++ % COLOURS.length]);
            ProcessorConsumer consumer = new ProcessorConsumer(app().getProcessor().getHazelcast(), processor.getId());
            processorTails.put(processor.getId(), consumer.getTailSequence());
            processorCommited.put(processor.getId(), consumer.getSequence());
        }
        Map<Integer, ProcessorRegistration> pools = new HashMap<>();
        Map<UUID, Integer> poolCounts = new HashMap<>();
        for (SchedulingPoolElector pool : app().getProcessor().getPoolElectors())
        {
            UUID leader = pool.getElectionMember(level);
            pools.put(pool.getPool(), processors.get(leader));
            poolCounts.merge(leader, 1, (a, b) -> a + b);
        }
        List<Integer> levels = new ArrayList<>();
        for (int i = 0; i < processors.size(); i++)
        {
            levels.add(i);
        }
        for (NotifierRegistration notifier : notifiers)
        {
            // TODO: we should have a task computing real stats somewhere
            
        }
        var("processors", processors.values());
        var("processor_colours", colours);
        var("processor_pools", poolCounts);
        var("processor_leaders", app().getProcessor().getLeaderElector().getElectionMembers());
        var("processor_tail", processorTails);
        var("processor_commited", processorCommited);
        // pools
        var("pools", pools.entrySet());
        var("levels", levels);
        // list sites
        List<Site> sites = var("sites", db.listSites());
        // list global admins
        var("globalAdmins", db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS).getParameters().keySet().stream().map((id) -> db.getContact(UUID.fromString(id))).filter((c) -> c != null).collect(Collectors.toList()));
        // list all admins
        List<Contact> allAdmins = new LinkedList<Contact>();
        for (Site site : sites)
        {
            if (! site.isDisabled())
            {
                for (Contact contact : db.listContacts(site.getId()))
                {
                    if (contact.hasPermission("ui.admin") && (! contact.isGlobalAdmin())) 
                        allAdmins.add(contact);
                }
            }
        }
        var("allAdmins", allAdmins);
        // render
        encode("/global/admin/index");
    }
    
    @Any("/site/id/:id/disable")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void disableSite(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        // get the site 
        Site site = notNull(db.getSite(id));
        logger.warn("Disabling site: " + site.getName() + " (" + site.getId() + ")"); 
        // mark the site as disabled
        site.setDisabled(true);
        db.setSite(site);
        // deinit the site from scheduling
        action("site-deinit", site);
        redirect(path("/global/admin/"));
    }
    
    @Any("/site/id/:id/enable")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void enableSite(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        // get the site 
        Site site = notNull(db.getSite(id));
        logger.warn("Enabling site: " + site.getName() + " (" + site.getId() + ")");
        // mark the site as enabled
        site.setDisabled(false);
        db.setSite(site);
        // init the site for scheduling
        action("site-init", site);
        redirect(path("/global/admin/"));
    }
    
    @Any("/contact/id/:id/disable")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void disableContact(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        Contact contact = notNull(db.getContact(id));
        GlobalSetting globalAdmins = db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
        globalAdmins.setParameter(contact.getId().toString(), "false");
        db.setGlobalSetting(globalAdmins);
        redirect(path("/global/admin/"));
    }
    
    @Any("/contact/id/:id/enable")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void enableContact(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        Contact contact = notNull(db.getContact(id));
        GlobalSetting globalAdmins = db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
        globalAdmins.setParameter(contact.getId().toString(), "true");
        db.setGlobalSetting(globalAdmins);
        redirect(path("/global/admin/"));
    }
    
    @Any("/contact/id/:id/remove")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void removeContact(BergamotDB db, @IsaUUID UUID id) throws Exception
    {
        Contact contact = notNull(db.getContact(id));
        GlobalSetting globalAdmins = db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
        globalAdmins.removeParameter(contact.getId().toString());
        db.setGlobalSetting(globalAdmins);
        redirect(path("/global/admin/"));
    }
    
    @Any("/contact/add")
    @RequireValidAccessTokenForURL(@Param("access-token"))
    @WithDataAdapter(BergamotDB.class)
    public void addContact(BergamotDB db, @IsaUUID @Param("id") UUID id) throws Exception
    {
        Contact contact = notNull(db.getContact(id));
        GlobalSetting globalAdmins = db.getGlobalSetting(GlobalSetting.NAME.GLOBAL_ADMINS);
        globalAdmins.setParameter(contact.getId().toString(), "true");
        db.setGlobalSetting(globalAdmins);
        redirect(path("/global/admin/"));
    }
}
