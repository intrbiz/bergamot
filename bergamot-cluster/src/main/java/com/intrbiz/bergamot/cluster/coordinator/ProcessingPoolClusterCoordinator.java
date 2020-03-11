package com.intrbiz.bergamot.cluster.coordinator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.InitialMembershipEvent;
import com.hazelcast.core.InitialMembershipListener;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.query.Predicates;
import com.intrbiz.bergamot.cluster.ObjectNames;
import com.intrbiz.bergamot.cluster.broker.SiteEventTopic;
import com.intrbiz.bergamot.cluster.coordinator.task.ProcessingPoolTask;
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
@SuppressWarnings("deprecation")
public class ProcessingPoolClusterCoordinator extends ProcessingPoolCoordinator
{
    private static final Logger logger = Logger.getLogger(ProcessingPoolClusterCoordinator.class);
    
    private final SiteEventTopic siteEventBroker;
    
    /**
     * The lock used when making decisions about managing the cluster. The general principle is that the first node to acquire the lock makes the decision about where resources run.
     */
    private final ILock clusterManagerLock;

    /**
     * A queue of migration events we must apply
     */
    private IQueue<ProcessingPoolTask> migrations;

    private volatile boolean runMigrations = false;

    private Thread migrationsConsumer;
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    @SuppressWarnings("unused")
    private String membershipListenerId = null;
    
    @SuppressWarnings("unused")
    private String siteEventListenerId = null;
    
    /**
     * Listener for processing pool actions
     */
    private ProcessingPoolListener poolListener;

    public ProcessingPoolClusterCoordinator(HazelcastInstance hazelcastInstance, SiteEventTopic siteEventBroker)
    {
        super(hazelcastInstance);
        this.siteEventBroker = Objects.requireNonNull(siteEventBroker);
        // setup our data structures
        this.clusterManagerLock = this.hazelcastInstance.getLock(ObjectNames.getClusterManagerLock());
        // listen to site events
        this.siteEventListenerId = this.siteEventBroker.listen(this::handleSiteEvent);
    }

    public UUID getId()
    {
        return UUID.fromString(this.cluster.getLocalMember().getUuid());
    }
    
    public ClusterInfo info()
    {
        // describe the current state of this cluster
        try
        {
            ClusterInfo ci = new ClusterInfo(this.cluster.getLocalMember().getUuid(), this.cluster.getLocalMember().getAddress().getInetAddress().getHostAddress());
            for (Member member : this.cluster.getMembers())
            {
                if (isProcessingPool(member))
                {
                    ProcessorInfo mi = new ProcessorInfo(member.getUuid(), member.getAddress().getInetAddress().getHostAddress());
                    // pools for this member
                    for (ProcessingPoolRegistration pool : this.pools.values(new OwnerPredicate(UUID.fromString(member.getUuid()))))
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
    
    public synchronized void stopProcessingPool()
    {
        if (this.started.compareAndSet(true, false))
        {
            // Mark our local member as not a processing node
            this.cluster.getLocalMember().setBooleanAttribute(ObjectNames.Attributes.MEMBER_TYPE_PROCESSOR, false);
        }
    }

    public synchronized ProcessingPoolConsumer startProcessingPool(ProcessingPoolListener poolListener)
    {
        Objects.requireNonNull(poolListener);
        if (this.started.compareAndSet(false, true))
        {
            this.poolListener = poolListener;
            // listen to cluster state changes
            this.membershipListenerId = this.cluster.addMembershipListener(new ProcessingPoolMembershipListener());
            // Mark our local member as a processing node
            this.cluster.getLocalMember().setBooleanAttribute(ObjectNames.Attributes.MEMBER_TYPE_PROCESSOR, true);
            // setup our migration task consumer
            this.migrations = this.getMigrationQueue(UUID.fromString(this.cluster.getLocalMember().getUuid()));
            this.runMigrations = true;
            this.migrationsConsumer = new Thread(this::migrationRunner, "BergamotClusterMigrator");
            this.migrationsConsumer.start();
        }
        return new ProcessingPoolConsumer(this.hazelcastInstance, UUID.fromString(this.cluster.getLocalMember().getUuid()));
    }
    
    private void sendMigration(Member runOn, ProcessingPoolTask migration)
    {
        if (runOn != null)
        {
            try
            {
                this.getMigrationQueue(UUID.fromString(runOn.getUuid())).put(migration);
            }
            catch (Exception e)
            {
                logger.fatal("Failed to queue cluster migration task, cluster could be inconsistent!", e);
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
        logger.info("Starting cluster migrations thread");
        while (this.runMigrations)
        {
            try
            {
                ProcessingPoolTask task = this.migrations.poll(10, TimeUnit.SECONDS);
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
        logger.info("Terminating cluster migrations thread");
    }
    
    private void fireSchedulerAction(SchedulerAction action)
    {
        if (this.poolListener != null && action != null)
        {
            this.poolListener.handleSchedulerAction(action);
        }
    }
    
    private void fireRegisterPool(UUID siteId, int processingPool)
    {
        if (this.poolListener != null)
        {
            this.poolListener.registerPool(siteId, processingPool);
        }
    }
    
    private void fireDeregisterPool(UUID siteId, int processingPool)
    {
        if (this.poolListener != null)
        {
            this.poolListener.deregisterPool(siteId, processingPool);
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
            Member[] processingMembers = memberSet.stream().filter(ProcessingPoolClusterCoordinator::isProcessingPool).toArray(Member[]::new);
            for (int i = 0; i < Site.PROCESSING_POOL_COUNT; i++)
            {
                ProcessingPoolRegistration pool = new ProcessingPoolRegistration(site.getId(), i);
                Member owner = processingMembers[Math.abs(pool.getPool() % processingMembers.length)];
                pool.owner(UUID.fromString(owner.getUuid()));
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
    private void takeOverPools(Member local, Member removed, Set<Member> memberSet)
    {
        this.clusterManagerLock.lock();
        try
        {
            // reassign pools
            logger.info("Taking over pools from member: " + removed);
            Member[] processingMembers = memberSet.stream().filter(ProcessingPoolClusterCoordinator::isProcessingPool).toArray(Member[]::new);
            Set<ProcessingPoolRegistration> altered = new HashSet<ProcessingPoolRegistration>();
            for (ProcessingPoolRegistration pool : this.pools.values(new OwnerPredicate(UUID.fromString(removed.getUuid()))))
            {
                Member newOwner = processingMembers[Math.abs(pool.getPool() % processingMembers.length)];
                logger.trace("Redistributing pool " + pool.getKey() + " from " + pool.getOwner() + " to " + newOwner.getUuid());
                pool.migrate(UUID.fromString(newOwner.getUuid()));
                this.pools.put(pool.getKey(), pool);
                altered.add(pool);
            }
            logger.info("Taken over " + altered.size() + " pools");
            // setup pools
            this.registerPools(altered, memberSet);
            // clear the queue of the removed member
            this.clearQueue(UUID.fromString(removed.getUuid()));
        }
        finally
        {
            this.clusterManagerLock.unlock();
        }
    }

    /**
     * Release some pools for the new member to handle
     */
    private void giveUpPools(Member local, Member added, Set<Member> memberSet)
    {
        this.clusterManagerLock.lock();
        try
        {
            Member[] processingMembers = memberSet.stream().filter(ProcessingPoolClusterCoordinator::isProcessingPool).toArray(Member[]::new);
            if (processingMembers.length > 0)
            {
                int poolsPerNode = Math.max(this.pools.size() / processingMembers.length, 1);
                // reassign pools
                logger.info("Giving up pools to member: " + added);
                Set<ProcessingPoolRegistration> altered = new HashSet<ProcessingPoolRegistration>();
                for (ProcessingPoolRegistration pool : this.pools.values(Predicates.not(new OwnerPredicate(UUID.fromString(added.getUuid())))))
                {
                    logger.trace("Redistributing pool " + pool.getKey() + " from " + pool.getOwner() + " to " + added.getUuid());
                    pool.migrate(UUID.fromString(added.getUuid()));
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
        Map<UUID, Member> members = memberSet.stream().collect(Collectors.toMap((m) -> UUID.fromString(m.getUuid()), (m) -> m));
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
        Map<UUID, Member> members = memberSet.stream().collect(Collectors.toMap((m) -> UUID.fromString(m.getUuid()), (m) -> m));
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
    
    private class ProcessingPoolMembershipListener implements InitialMembershipListener
    {
        @Override
        public void memberAdded(MembershipEvent membershipEvent)
        {
            // when a member is added we will release some processing pools to it
            logger.info("Member added: " + membershipEvent.getMember() + " members: " + membershipEvent.getMembers());
            if (isProcessingPool(membershipEvent.getMember()))
            {
                logger.info("Member is a processing node: " + membershipEvent.getMember());
                giveUpPools(membershipEvent.getCluster().getLocalMember(), membershipEvent.getMember(), membershipEvent.getMembers());
            }
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent)
        {
            // when a member is removed from the cluster we will redistribute its pools over the cluster
            logger.info("Member removed: " + membershipEvent.getMember() + " members: " + membershipEvent.getMembers());
            takeOverPools(membershipEvent.getCluster().getLocalMember(), membershipEvent.getMember(), membershipEvent.getMembers());
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent membershipEvent)
        {
            logger.info("Member attributes changed: " + membershipEvent.getMember());
            if (ObjectNames.Attributes.MEMBER_TYPE_PROCESSOR.equals(membershipEvent.getKey()))
            {
                if (Boolean.TRUE.equals(membershipEvent.getValue()))
                {
                    logger.info("Member is a processing node: " + membershipEvent.getMember());
                    giveUpPools(membershipEvent.getCluster().getLocalMember(), membershipEvent.getMember(), membershipEvent.getMembers());
                }
                else
                {
                    logger.info("Member was a processing node: " + membershipEvent.getMember());
                    takeOverPools(membershipEvent.getCluster().getLocalMember(), membershipEvent.getMember(), membershipEvent.getMembers());
                }
            }
        }

        @Override
        public void init(InitialMembershipEvent event)
        {
        }
    }
}
