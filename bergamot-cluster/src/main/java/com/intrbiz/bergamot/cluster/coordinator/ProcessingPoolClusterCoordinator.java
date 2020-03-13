package com.intrbiz.bergamot.cluster.coordinator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.collection.IQueue;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.query.Predicates;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.broker.SiteEventTopic;
import com.intrbiz.bergamot.cluster.coordinator.task.ProcessingPoolTask;
import com.intrbiz.bergamot.cluster.coordinator.task.ProcessorTask;
import com.intrbiz.bergamot.cluster.listener.ProcessingPoolListener;
import com.intrbiz.bergamot.cluster.model.ProcessingPoolRegistration;
import com.intrbiz.bergamot.cluster.model.info.ClusterInfo;
import com.intrbiz.bergamot.cluster.model.info.PoolInfo;
import com.intrbiz.bergamot.cluster.model.info.ProcessorInfo;
import com.intrbiz.bergamot.cluster.queue.ProcessingPoolConsumer;
import com.intrbiz.bergamot.cluster.util.OwnerPredicate;
import com.intrbiz.bergamot.cluster.util.SitePredicate;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.model.message.event.site.DeinitSite;
import com.intrbiz.bergamot.model.message.event.site.InitSite;
import com.intrbiz.bergamot.model.message.event.site.SiteEvent;
import com.intrbiz.bergamot.model.message.scheduler.SchedulerAction;

/**
 * Manage scheduling and result processing services across the cluster
 * 
 * Every site has a (configurable) number of processing pools, checks 
 * are split across these pools. The processing pools are then split 
 * across the members within the cluster. As such work should be 
 * balanced across the scheduling and result processing resources 
 * of the cluster.
 * 
 * This cluster manager is handle migrating resources around the 
 * cluster. The general idea is that when the cluster state changes, 
 * the cluster members will race to acquire the management lock. 
 * The winner will then decide the state of the cluster and issue 
 * the required migration tasks.
 * 
 * Migrations are placed into a queue named with the UUID of the 
 * cluster member which should execute them.  A cluster member will 
 * consume and apply these migration tasks
 * 
 */
public class ProcessingPoolClusterCoordinator extends ProcessingPoolCoordinator
{
    private static final Logger logger = Logger.getLogger(ProcessingPoolClusterCoordinator.class);
    
    /**
     * Broker to listen for site events
     */
    private final SiteEventTopic siteEventBroker;
    
    /**
     * CP subsystem used for locking and pool management
     */
    private final CPSubsystem cpSubsytem;
    
    /**
     * The lock used when making decisions about managing the cluster. The general principle is that the first node to acquire the lock makes the decision about where resources run.
     */
    private final FencedLock clusterManagerLock;
    
    /**
     * Queue for processor registrations
     */
    private IQueue<ProcessorTask> registrations;
    
    private AtomicBoolean runRegistrations;
    
    private Thread registrationsRunner;

    /**
     * A queue of migration events we must apply
     */
    private IQueue<ProcessingPoolTask> migrations;

    private AtomicBoolean runMigrations;

    private Thread migrationsConsumer;
    
    /**
     * Latch to wait for all threads to shutdown
     */
    private CountDownLatch shutdownLatch;
    
    /**
     * State tracking
     */
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    /**
     * Id for the cluster membership listener
     */
    private UUID membershipListenerId = null;
    
    /**
     * Id for the site event listener
     */
    private UUID siteEventListenerId = null;
    
    /**
     * Listeners for processing pool actions
     */
    private ConcurrentMap<UUID, ProcessingPoolListener> poolListeners = new ConcurrentHashMap<>();

    public ProcessingPoolClusterCoordinator(HazelcastInstance hazelcastInstance, SiteEventTopic siteEventBroker)
    {
        super(hazelcastInstance);
        this.siteEventBroker = Objects.requireNonNull(siteEventBroker);
        // setup our data structures
        this.cpSubsytem = this.hazelcastInstance.getCPSubsystem();
        this.clusterManagerLock = this.cpSubsytem.getLock(ObjectNames.getClusterManagerLock());
        // listen to site events
        this.siteEventListenerId = this.siteEventBroker.listen(this::handleSiteEvent);
    }

    @Override
    protected void configureHazelcast(Config hazelcastConfig)
    {
        // Configure the processors map
        MapConfig processorsMap = hazelcastConfig.getMapConfig(ObjectNames.buildProcessorsMapName());
        processorsMap.setBackupCount(2);
        processorsMap.setAsyncBackupCount(0);
        // Configure the processing pools map
        MapConfig processingPoolsMap = hazelcastConfig.getMapConfig(ObjectNames.buildProcessingPoolsMapName());
        processingPoolsMap.setBackupCount(2);
        processingPoolsMap.setAsyncBackupCount(0);
    }

    public UUID getId()
    {
        return this.cluster.getLocalMember().getUuid();
    }
    
    public ClusterInfo info()
    {
        // describe the current state of this cluster
        try
        {
            ClusterInfo ci = new ClusterInfo(this.cluster.getLocalMember().getUuid(), this.cluster.getLocalMember().getAddress().getInetAddress().getHostAddress());
            for (Member member : this.cluster.getMembers())
            {
                if (this.isProcessingPool(member))
                {
                    ProcessorInfo mi = new ProcessorInfo(member.getUuid(), member.getAddress().getInetAddress().getHostAddress());
                    // pools for this member
                    for (ProcessingPoolRegistration pool : this.pools.values(new OwnerPredicate(member.getUuid())))
                    {
                        mi.getPools().add(new PoolInfo(pool.getSite(), pool.getPool()));
                    }
                    ci.getProcessors().add(mi);
                }
            }
            return ci;
        }
        catch (Exception e)
        {
            logger.error("Failed to describe cluster", e);
        }
        return null;
    }
    
    public UUID listen(ProcessingPoolListener listener)
    {
        UUID id = UUID.randomUUID();
        this.poolListeners.put(id,  listener);
        return id;
    }
    
    public void unlisten(UUID id)
    {
        this.poolListeners.remove(id);
    }
    
    public ProcessingPoolConsumer createConsumer()
    {
        return new ProcessingPoolConsumer(this.hazelcastInstance, this.cluster.getLocalMember().getUuid());
    }
    
    public void stop()
    {
        if (this.started.compareAndSet(true, false))
        {
            // Shutdown our registration and migrations threads
            this.runRegistrations.set(false);
            this.runMigrations.set(false);
            try
            {
                this.shutdownLatch.await();
            }
            catch (InterruptedException e)
            {
            }
            // Unregister event listener
            this.cluster.removeMembershipListener(this.membershipListenerId);
            this.siteEventBroker.unlisten(this.siteEventListenerId);
            // Unregister as a processor
            this.registrations.offer(new ProcessorTask(ProcessorTask.Action.REGISTER, this.cluster.getLocalMember().getUuid()));
        }
    }

    public void start()
    {
        if (this.started.compareAndSet(false, true))
        {
            this.shutdownLatch = new CountDownLatch(2);
            // listen to cluster state changes
            this.membershipListenerId = this.cluster.addMembershipListener(new ProcessingPoolMembershipListener());
            // setup our processor registration task
            this.registrations = this.hazelcastInstance.getQueue(ObjectNames.buildProcessorRegistrationQueue());
            this.runRegistrations = new AtomicBoolean(true);
            this.registrationsRunner = new Thread(this::registrationRunner, "bergamot-processor-registrations");
            this.registrationsRunner.start();
            // setup our migrations task
            this.migrations = this.getMigrationQueue(this.cluster.getLocalMember().getUuid());
            this.runMigrations = new AtomicBoolean(true);
            this.migrationsConsumer = new Thread(this::migrationRunner, "bergamot-processor-migrations");
            this.migrationsConsumer.start();
            // register as a processor
            this.registrations.offer(new ProcessorTask(ProcessorTask.Action.REGISTER, this.cluster.getLocalMember().getUuid()));
        }
    }
    
    private void registrationRunner()
    {
        logger.info("Starting processor registration thread");
        while (this.runRegistrations.get())
        {
            try
            {
                ProcessorTask task = this.registrations.poll(1, TimeUnit.SECONDS);
                if (task != null)
                {
                    try
                    {
                        switch (task.getAction())
                        {
                            case DEREGISTER:
                                logger.info("Member is deregistering as a processing node: " + task.getId());
                                this.processors.remove(task.getId());
                                takeOverPools(task.getId(), this.cluster.getMembers());
                                break;
                            case REGISTER:
                                logger.info("Member is registering as a processing node: " + task.getId());
                                this.processors.put(task.getId(), Boolean.TRUE);
                                giveUpPools(task.getId(), this.cluster.getMembers());
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Error processor registration", e);
                    }
                }
            }
            catch (InterruptedException e)
            {
            }
        }
        logger.info("Terminating processor registration thread");
        this.shutdownLatch.countDown();
    }
    
    private void sendMigration(Member runOn, ProcessingPoolTask migration)
    {
        if (runOn != null)
        {
            try
            {
                this.getMigrationQueue(runOn.getUuid()).put(migration);
            }
            catch (Exception e)
            {
                logger.fatal("Failed to queue processor migration task, cluster could be inconsistent!", e);
            }
        }
    }

    private IQueue<ProcessingPoolTask> getMigrationQueue(UUID memberUUID)
    {
        return this.hazelcastInstance.getQueue(ObjectNames.buildClusterMigrationQueueName(memberUUID));
    }

    private void clearQueue(UUID memberUUID)
    {
        this.getMigrationQueue(memberUUID).destroy();
    }
    
    private void migrationRunner()
    {
        logger.info("Starting processor migrations thread");
        while (this.runMigrations.get())
        {
            try
            {
                ProcessingPoolTask task = this.migrations.poll(1, TimeUnit.SECONDS);
                if (task != null)
                {
                    try
                    {
                        switch (task.getAction())
                        {
                            case DEREGISTER:
                                this.fireDeregisterPool(task.getSite(), task.getPool());
                                break;
                            case REGISTER:
                                this.fireRegisterPool(task.getSite(), task.getPool());
                                break;
                            case FIRE:
                                this.fireSchedulerAction(task.getSchedulerAction());
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Error executing pool migration task", e);
                    }
                }
            }
            catch (InterruptedException e)
            {
            }
        }
        logger.info("Terminating processor migrations thread");
        this.shutdownLatch.countDown();
    }
    
    private void fireSchedulerAction(SchedulerAction action)
    {
        if (action != null)
        {
            for (ProcessingPoolListener listener : this.poolListeners.values())
            {
                listener.handleSchedulerAction(action);
            }
        }
    }
    
    private void fireRegisterPool(UUID siteId, int processingPool)
    {
        for (ProcessingPoolListener listener : this.poolListeners.values())
        {
            listener.registerPool(siteId, processingPool);
        }
    }
    
    private void fireDeregisterPool(UUID siteId, int processingPool)
    {
        for (ProcessingPoolListener listener : this.poolListeners.values())
        {
            listener.deregisterPool(siteId, processingPool);
        }
    }

    public void registerSite(Site site)
    {
        logger.info("Registering site " + site.getId() + " " + site.getName());
        this.initPoolsForSite(this.cluster.getMembers(), site);
    }
    
    public void deregisterSite(Site site)
    {
        logger.info("Deregistering site " + site.getId() + " " + site.getName());
        this.deinitPoolsForSite(this.cluster.getMembers(), site);
    }
    
    /**
     * Remove all pools for a site
     */
    private void deinitPoolsForSite(Set<Member> memberSet, Site site)
    {
        this.clusterManagerLock.lock();
        try
        {
            // add the pools for the given site
            logger.info("Uninitialising cluster state, members: " + memberSet);
            // get all the processing pools for this site
            for (ProcessingPoolRegistration pool : this.pools.values(new SitePredicate(site.getId())))
            {
                // ensure the processing pool is removed
                for (Member member : memberSet)
                {
                    this.sendMigration(member, new ProcessingPoolTask(ProcessingPoolTask.Action.DEREGISTER, pool.getSite(), pool.getPool()));
                }
            }
        }
        finally
        {
            this.clusterManagerLock.unlock();
        }
    }

    /**
     * Initially register the pools for the given site with this cluster.
     */
    private void initPoolsForSite(Set<Member> memberSet, Site site)
    {
        this.clusterManagerLock.lock();
        try
        {
            // add the pools for the given site
            logger.info("Initialising cluster state, members: " + memberSet);
            Set<ProcessingPoolRegistration> altered = new HashSet<ProcessingPoolRegistration>();
            Member[] processingMembers = memberSet.stream().filter(this::isProcessingPool).toArray(Member[]::new);
            for (int i = 0; i < Site.PROCESSING_POOL_COUNT; i++)
            {
                ProcessingPoolRegistration pool = new ProcessingPoolRegistration(site.getId(), i);
                Member owner = processingMembers[Math.abs(pool.getPool() % processingMembers.length)];
                pool.owner(owner.getUuid());
                ProcessingPoolRegistration previous = this.pools.putIfAbsent(pool.getKey(), pool);
                logger.trace("Pool " + pool.getKey() + " owner " + owner.getUuid());
                if (previous == null) altered.add(pool);
            }
            logger.info("Assigned " + altered.size() + " pools");
            // setup our pools
            this.registerPools(altered, memberSet);
        }
        finally
        {
            this.clusterManagerLock.unlock();
        }
    }

    /**
     * Take over pools from a failed member
     */
    private void takeOverPools(UUID removed, Set<Member> memberSet)
    {
        this.clusterManagerLock.lock();
        try
        {
            // reassign pools
            logger.info("Taking over pools from member: " + removed);
            Member[] processingMembers = memberSet.stream().filter(this::isProcessingPool).toArray(Member[]::new);
            Set<ProcessingPoolRegistration> altered = new HashSet<ProcessingPoolRegistration>();
            for (ProcessingPoolRegistration pool : this.pools.values(new OwnerPredicate(removed)))
            {
                Member newOwner = processingMembers[Math.abs(pool.getPool() % processingMembers.length)];
                logger.trace("Redistributing pool " + pool.getKey() + " from " + pool.getOwner() + " to " + newOwner.getUuid());
                pool.migrate(newOwner.getUuid());
                this.pools.put(pool.getKey(), pool);
                altered.add(pool);
            }
            logger.info("Taken over " + altered.size() + " pools");
            // setup pools
            this.registerPools(altered, memberSet);
            // clear the queue of the removed member
            this.clearQueue(removed);
        }
        finally
        {
            this.clusterManagerLock.unlock();
        }
    }

    /**
     * Release some pools for the new member to handle
     */
    private void giveUpPools(UUID added, Set<Member> memberSet)
    {
        this.clusterManagerLock.lock();
        try
        {
            Member[] processingMembers = memberSet.stream().filter(this::isProcessingPool).toArray(Member[]::new);
            if (processingMembers.length > 0)
            {
                int poolsPerNode = Math.max(this.pools.size() / processingMembers.length, 1);
                // reassign pools
                logger.info("Giving up pools to member: " + added);
                Set<ProcessingPoolRegistration> altered = new HashSet<ProcessingPoolRegistration>();
                for (ProcessingPoolRegistration pool : this.pools.values(Predicates.not(new OwnerPredicate(added))))
                {
                    logger.trace("Redistributing pool " + pool.getKey() + " from " + pool.getOwner() + " to " + added);
                    pool.migrate(added);
                    this.pools.put(pool.getKey(), pool);
                    altered.add(pool);
                    // have we reassigned enough pools
                    if (altered.size() >= poolsPerNode) break;
                }
                logger.info("Released " + altered.size() + " pools");
                // setup pools
                this.registerPools(altered, memberSet);
                this.deregisterPools(altered, memberSet);
            }
        }
        finally
        {
            this.clusterManagerLock.unlock();
        }
    }

    /**
     * Execute the register pool tasks on the various cluster members
     */
    private void registerPools(Collection<ProcessingPoolRegistration> pools, Set<Member> memberSet)
    {
        Map<UUID, Member> members = memberSet.stream().collect(Collectors.toMap((m) -> m.getUuid(), (m) -> m));
        for (ProcessingPoolRegistration pool : pools)
        {
            this.sendMigration(members.get(pool.getOwner()), new ProcessingPoolTask(ProcessingPoolTask.Action.REGISTER, pool.getSite(), pool.getPool()));
        }
    }

    /**
     * Execute the deregister pool tasks of the various cluster members
     */
    private void deregisterPools(Collection<ProcessingPoolRegistration> pools, Set<Member> memberSet)
    {
        Map<UUID, Member> members = memberSet.stream().collect(Collectors.toMap((m) -> m.getUuid(), (m) -> m));
        for (ProcessingPoolRegistration pool : pools)
        {
            this.sendMigration(members.get(pool.getPreviousOwner()), new ProcessingPoolTask(ProcessingPoolTask.Action.DEREGISTER, pool.getSite(), pool.getPool()));
        }
    }

    private void handleSiteEvent(SiteEvent event)
    {
        try
        {
            if (event instanceof InitSite)
            {
                UUID siteId = ((InitSite) event).getSiteId();
                String siteName = ((InitSite) event).getSiteName();
                logger.info("Got request to init site: " + siteId + " - " + siteName);
                try (BergamotDB db = BergamotDB.connect())
                {
                    Site site = db.getSite(siteId);
                    if (site != null)
                        this.registerSite(site);
                }
            }
            else if (event instanceof DeinitSite)
            {
                UUID siteId = ((DeinitSite) event).getSiteId();
                String siteName = ((DeinitSite) event).getSiteName();
                logger.info("Got request to deinit site: " + siteId + " - " + siteName);
                try (BergamotDB db = BergamotDB.connect())
                {
                    Site site = db.getSite(siteId);
                    if (site != null)
                        this.deregisterSite(site);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to process site event", e);
        }
    }
    
    public ProcessingPoolRegistration getProcessPoolForCheck(UUID objectId)
    {
        return this.pools.get(Site.getSiteProcessingPool(objectId));
    }
    
    public ProcessingPoolRegistration getProcessPoolForSite(UUID siteId, int processingPool)
    {
        return this.pools.get(Site.getSiteProcessingPool(siteId, processingPool));
    }
    
    public int getMemberCount()
    {
        return this.hazelcastInstance.getCluster().getMembers().size();
    }
    
    public int getProcessPoolCount()
    {
        return this.pools == null ? 0 : this.pools.size();
    }
    
    private class ProcessingPoolMembershipListener implements MembershipListener
    {
        @Override
        public void memberAdded(MembershipEvent membershipEvent)
        {
            // when a member is added we will release some processing pools to it
            logger.info("Member added: " + membershipEvent.getMember() + " members: " + membershipEvent.getMembers());
            if (isProcessingPool(membershipEvent.getMember()))
            {
                logger.info("Member is a processing node: " + membershipEvent.getMember());
                giveUpPools(membershipEvent.getMember().getUuid(), membershipEvent.getMembers());
            }
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent)
        {
            // when a member is removed from the cluster we will redistribute its pools over the cluster
            logger.info("Member removed: " + membershipEvent.getMember() + " members: " + membershipEvent.getMembers());
            takeOverPools(membershipEvent.getMember().getUuid(), membershipEvent.getMembers());
        }
    }
}
