package com.intrbiz.bergamot.cluster;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.coordinator.WorkerSchedulerCoordinator;
import com.intrbiz.bergamot.data.BergamotDB;
import com.intrbiz.bergamot.model.Site;
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
public class ProcessingPoolManager
{
    private static final Logger logger = Logger.getLogger(ProcessingPoolManager.class);
    
    private Config hazelcastConfig;

    private HazelcastInstance hazelcastInstance;

    private volatile boolean started = false;
    
    private Cluster cluster;
    
    private WorkerSchedulerCoordinator workerCoordinator;
    
    private ProcessingPool processingPool;

    public ProcessingPoolManager()
    {
        super();
    }

    public synchronized void start()
    {
        try
        {
            if (!this.started)
            {
                this.started = true;
                // start hazelcast
                if (this.hazelcastConfig == null)
                {
                    // setup config
                    String hazelcastConfigFile = Util.coalesceEmpty(System.getProperty("hazelcast.config"), System.getenv("hazelcast_config"));
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
                // create the hazelcast instance
                logger.info("Using Hazelcast configuration: " + this.hazelcastConfig);
                this.hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(this.hazelcastConfig);
                this.cluster = this.hazelcastInstance.getCluster();
                // Start our coordinators
                this.workerCoordinator = new WorkerSchedulerCoordinator(this.hazelcastInstance);
                this.processingPool = new ProcessingPool(this.workerCoordinator, cluster.getLocalMember());
                // Start our processing pool
                this.processingPool.start();
                // Register sites with the processing pool
                this.registerSites();
            }
        }
        catch (Exception e)
        {
            throw new DataException("Failed to start Processing Pool Manager", e);
        }
    }
    
    protected void registerSites()
    {
        try (BergamotDB db = BergamotDB.connect())
        {
            for (Site site : db.listSites())
            {
                if (! site.isDisabled())
                {
                    this.workerCoordinator.registerSite(site);
                }
            }
        }
    }
    
    public void shutdown()
    {
    }
    
    public int getMemberCount()
    {
        return this.cluster.getMembers().size();
    }
}
