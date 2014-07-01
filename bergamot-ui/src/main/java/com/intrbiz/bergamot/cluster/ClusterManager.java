package com.intrbiz.bergamot.cluster;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.InitialMembershipEvent;
import com.hazelcast.core.InitialMembershipListener;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.query.Predicates;
import com.intrbiz.bergamot.model.Site;
import com.intrbiz.bergamot.result.DefaultResultProcessor;
import com.intrbiz.bergamot.result.ResultProcessor;
import com.intrbiz.bergamot.scheduler.Scheduler;
import com.intrbiz.bergamot.scheduler.WheelScheduler;
import com.intrbiz.data.DataException;

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
public class ClusterManager
{
    private Config hazelcastConfig;

    private HazelcastInstance hazelcastInstance;

    private Logger logger = Logger.getLogger(ClusterManager.class);

    /**
     * The lock used when making decisions about managing the cluster. The general principle is that the first node to acquire the lock makes the decision about where resources run.
     */
    private ILock clusterManagerLock;

    /**
     * The distributed map of processing pools
     */
    private IMap<String, ProcessingPool> pools;

    /**
     * A queue of migration events we must apply
     */
    private IQueue<ClusterMigration> migrations;

    private volatile boolean runMigrations = false;

    private Thread migrationsConsumer;

    private volatile boolean started = false;

    private Cluster cluster;

    /**
     * Our scheduler
     */
    private Scheduler scheduler;

    /**
     * Our result processor
     */
    private ResultProcessor processor;

    public ClusterManager()
    {
        super();
        this.scheduler = new WheelScheduler();
        this.processor = new DefaultResultProcessor();
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }

    public ResultProcessor getResultProcessor()
    {
        return this.processor;
    }

    public String getLocalMemberUUID()
    {
        return this.cluster.getLocalMember().getUuid();
    }

    public void start(String instanceName)
    {
        this.start(null, instanceName);
    }

    public synchronized void start(Config config, String instanceName)
    {
        try
        {
            if (!this.started)
            {
                this.started = true;
                // start hazelcast
                if (config == null)
                {
                    // setup config
                    String hazelcastConfigFile = System.getProperty("hazelcast.config");
                    if (hazelcastConfigFile != null)
                    {
                        // when using a config file, you must configure the balsa.sessions map
                        this.hazelcastConfig = new XmlConfigBuilder(hazelcastConfigFile).build();
                    }
                    else
                    {
                        // setup the default configuration
                        this.hazelcastConfig = new Config();
                    }
                }
                else
                {
                    this.hazelcastConfig = config;
                }
                // create the hazel cast instance
                if (instanceName == null)
                {
                    this.hazelcastInstance = Hazelcast.newHazelcastInstance(this.hazelcastConfig);
                }
                else
                {
                    // set the instance name
                    this.hazelcastConfig.setInstanceName(instanceName);
                    // create the instance
                    this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(this.hazelcastConfig);
                }
                // setup
                // setup data structures
                this.clusterManagerLock = this.hazelcastInstance.getLock("bergamot.cluster.manager");
                this.pools = this.hazelcastInstance.getMap("bergamot.pools");
                this.cluster = this.hazelcastInstance.getCluster();
                this.migrations = this.getMigrationQueue(this.cluster.getLocalMember().getUuid());
                // listen to cluster state changes
                this.cluster.addMembershipListener(new InitialMembershipListener()
                {
                    @Override
                    public void memberAdded(MembershipEvent membershipEvent)
                    {
                        // when a member is added we will release some processing pools to it
                        logger.info("Member added: " + membershipEvent.getMember() + " members: " + membershipEvent.getMembers());
                        giveUpPools(membershipEvent.getCluster().getLocalMember(), membershipEvent.getMember(), membershipEvent.getMembers());
                    }

                    @Override
                    public void memberRemoved(MembershipEvent membershipEvent)
                    {
                        // when a member is removed from the cluster we will redistribute its pools over the cluster
                        logger.info("Member removed: " + membershipEvent.getMember() + " members: " + membershipEvent.getMembers());
                        logger.info("Taking over pools from node");
                        takeOverPools(membershipEvent.getCluster().getLocalMember(), membershipEvent.getMember(), membershipEvent.getMembers());
                    }

                    @Override
                    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent)
                    {
                    }

                    @Override
                    public void init(InitialMembershipEvent event)
                    {
                    }
                });
                // start our scheduler and result processor
                logger.info("Starting result processor");
                this.processor.start();
                logger.info("Starting scheduler");
                this.scheduler.start();
                // setup our migration task consumer
                this.runMigrations = true;
                this.migrationsConsumer = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        logger.info("Starting cluster migrations thread");
                        while (runMigrations)
                        {
                            try
                            {
                                ClusterMigration migration = migrations.poll(10, TimeUnit.SECONDS);
                                if (migration != null)
                                {
                                    logger.info("Executing cluster migration: " + migration);
                                    boolean result = migration.applyMigration(ClusterManager.this);
                                    logger.debug("Migration completed: " + result);
                                }
                            }
                            catch (InterruptedException e)
                            {
                            }
                            catch (Exception e)
                            {
                                logger.error("Error applying migration", e);
                            }
                        }
                        logger.info("Terminating cluster migrations thread");
                    }
                }, "BergamotClusterMigrator");
                this.migrationsConsumer.start();
            }

        }
        catch (Exception e)
        {
            throw new DataException("Failed to start Hazelcast Cluster Manager", e);
        }
    }

    public void registerSite(Site site)
    {
        this.logger.info("Registering site " + site.getId() + " " + site.getName());
        this.initPoolsForSite(this.cluster.getLocalMember(), this.cluster.getMembers(), site);
    }

    /**
     * Initially register the pools for the given site with this cluster.
     */
    private void initPoolsForSite(Member local, Set<Member> memberSet, Site site)
    {
        this.clusterManagerLock.lock();
        try
        {
            // add the pools for the given site
            this.logger.info("Initialising cluster state, members: " + memberSet);
            Set<ProcessingPool> altered = new HashSet<ProcessingPool>();
            Member[] members = memberSet.stream().toArray((l) -> {
                return new Member[l];
            });
            for (int i = 0; i < site.getPoolCount(); i++)
            {
                ProcessingPool pool = new ProcessingPool(site.getId(), i);
                Member owner = members[Math.abs(pool.getKey().hashCode() % members.length)];
                pool.setOwner(owner.getUuid());
                ProcessingPool previous = this.pools.putIfAbsent(pool.getKey(), pool);
                this.logger.trace("Pool " + pool.getKey() + " owner " + owner.getUuid());
                if (previous == null) altered.add(pool);
            }
            this.logger.info("Assigned " + altered.size() + " pools");
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
            this.logger.info("Taking over pools from member: " + removed);
            Member[] members = memberSet.stream().toArray((l) -> {
                return new Member[l];
            });
            Set<ProcessingPool> altered = new HashSet<ProcessingPool>();
            for (ProcessingPool pool : this.pools.values(new OwnerPredicate(removed.getUuid())))
            {
                Member newOwner = members[Math.abs(pool.getKey().hashCode() % members.length)];
                this.logger.trace("Redistributing pool " + pool.getKey() + " from " + pool.getOwner() + " to " + newOwner.getUuid());
                pool.migrate(newOwner.getUuid());
                this.pools.put(pool.getKey(), pool);
                altered.add(pool);
            }
            this.logger.info("Taken over " + altered.size() + " pools");
            // setup pools
            this.registerPools(altered, memberSet);
            // clear the queue of the removed member
            this.clearQueue(removed.getUuid());
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
            int poolsPerNode = Math.max(this.pools.size() / memberSet.size(), 1);
            // reassign pools
            this.logger.info("Giving up pools to member: " + added);
            Set<ProcessingPool> altered = new HashSet<ProcessingPool>();
            for (ProcessingPool pool : this.pools.values(Predicates.not(new OwnerPredicate(added.getUuid()))))
            {
                this.logger.trace("Redistributing pool " + pool.getKey() + " from " + pool.getOwner() + " to " + added.getUuid());
                pool.migrate(added.getUuid());
                this.pools.put(pool.getKey(), pool);
                altered.add(pool);
                // have we reassigned enough pools
                if (altered.size() >= poolsPerNode) break;
            }
            this.logger.info("Released " + altered.size() + " pools");
            // setup pools
            this.registerPools(altered, memberSet);
            this.deregisterPools(altered, memberSet);
        }
        finally
        {
            this.clusterManagerLock.unlock();
        }
    }

    /**
     * Execute the register pool tasks on the various cluster members
     */
    private void registerPools(Collection<ProcessingPool> pools, Set<Member> memberSet)
    {
        Map<String, Member> members = memberSet.stream().collect(Collectors.toMap((m) -> {
            return m.getUuid();
        }, (m) -> {
            return m;
        }));
        for (ProcessingPool pool : pools)
        {
            Member runOn = members.get(pool.getOwner());
            if (runOn != null)
            {
                try
                {
                    this.getMigrationQueue(runOn.getUuid()).put(new RegisterPoolTask(pool.getSite(), pool.getPool()));
                }
                catch (InterruptedException e)
                {
                    logger.fatal("Failed to queue cluster migration task, cluster could be inconsistent!", e);
                }
            }
            else
            {
                logger.debug("Cannot run on member: " + pool.getOwner());
            }
        }
    }

    /**
     * Execute the deregister pool tasks of the various cluster members
     */
    private void deregisterPools(Collection<ProcessingPool> pools, Set<Member> memberSet)
    {
        Map<String, Member> members = memberSet.stream().collect(Collectors.toMap((m) -> {
            return m.getUuid();
        }, (m) -> {
            return m;
        }));
        for (ProcessingPool pool : pools)
        {
            Member runOn = members.get(pool.getPreviousOwner());
            if (runOn != null)
            {
                try
                {
                    this.getMigrationQueue(runOn.getUuid()).put(new DeregisterPoolTask(pool.getSite(), pool.getPool()));
                }
                catch (InterruptedException e)
                {
                    logger.fatal("Failed to queue cluster migration task, cluster could be inconsistent!", e);
                }
            }
            else
            {
                logger.debug("Cannot run on member: " + pool.getPreviousOwner());
            }
        }
    }

    private IQueue<ClusterMigration> getMigrationQueue(String memberUUID)
    {
        return this.hazelcastInstance.getQueue("bergamot.cluster.migrations." + memberUUID);
    }

    private void clearQueue(String memberUUID)
    {
        this.getMigrationQueue(memberUUID).clear();
    }
}
