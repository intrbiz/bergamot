package com.intrbiz.bergamot.cluster.client;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.discovery.HazelcastZooKeeperDiscovery;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager.ZooKeeperState;
import com.intrbiz.bergamot.config.ClusterCfg;

/**
 * A Bergamot client daemon which connects to the cluster and provides services
 */
public abstract class BergamotClient
{
    private static final Logger logger = Logger.getLogger(BergamotClient.class);
    
    protected final ClusterCfg config;
    
    protected final UUID id;
    
    protected final String application;
    
    protected final String info;
    
    protected final String hostName;
    
    protected final Consumer<Void> onPanic;
    
    protected final ZooKeeperManager zooKeeper;
    
    protected final HazelcastInstance hazelcast;
    
    public BergamotClient(ClusterCfg config, Consumer<Void> onPanic, String application, String info, String hostName) throws Exception
    {
        super();
        this.config = Objects.requireNonNull(config);
        this.onPanic = Objects.requireNonNull(onPanic);
        this.id = UUID.randomUUID();
        this.application = application;
        this.info = info;
        this.hostName = hostName;
        // Connect
        logger.info("Connecting to Bergamot Cluster");
        this.zooKeeper = this.connectZooKeeper();
        this.zooKeeper.listen(this::zooKeeperListener);
        this.hazelcast = this.connectHazelcast();
        logger.info("Connected to Bergamot Cluster");
    }
    
    protected String getZooKeeperNodes()
    {
        return Util.coalesceEmpty(
            System.getenv("ZOOKEEPER_NODES"), 
            System.getProperty("zookeeper.nodes"), 
            this.config.getZooKeeper().stream().collect(Collectors.joining(","))
        );
    }
    
    protected HazelcastInstance connectHazelcast() throws Exception
    {
        logger.info("Connecting Hazelcast");
        ClientConfig cliCfg = HazelcastZooKeeperDiscovery.configureHazelcastClient(this.zooKeeper, new ClientConfig());
        return HazelcastClient.newHazelcastClient(cliCfg); 
    }
    
    protected ZooKeeperManager connectZooKeeper() throws Exception
    {
        logger.info("Connecting ZooKeeper");
        return new ZooKeeperManager(this.getZooKeeperNodes());
    }
    
    protected void zooKeeperListener(ZooKeeperState state)
    {
        if (state == ZooKeeperState.EXPIRED)
        {
            this.panic();
        }
    }
    
    protected void panic()
    {
        logger.fatal("PANIC - lost connection to cluster");
        this.onPanic.accept(null);
    }
    
    public UUID getId()
    {
        return this.id;
    }
    
    public ZooKeeperManager getZooKeeper()
    {
        return this.zooKeeper;
    }

    public HazelcastInstance getHazelcast()
    {
        return this.hazelcast;
    }

    public void close()
    {
        try
        {
            this.hazelcast.shutdown();
        }
        catch (Exception e)
        {
            logger.warn("Error shutting down Hazelcast", e);
        }
        try
        {
            this.zooKeeper.shutdown();
        }
        catch (Exception e)
        {
            logger.warn("Error shutting down ZooKeeper", e);
        }
    }
}
