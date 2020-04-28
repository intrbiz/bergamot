package com.intrbiz.bergamot.cluster.member;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.hazelcast.topic.TopicOverloadPolicy;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.discovery.HazelcastZooKeeperDiscovery;
import com.intrbiz.bergamot.cluster.util.HZNames;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager.ZooKeeperState;
import com.intrbiz.bergamot.config.ClusterCfg;

/**
 * A Bergamot member daemon which forms part of the core cluster
 */
public abstract class BergamotMember
{
    private static final Logger logger = Logger.getLogger(BergamotMember.class);
    
    protected final ClusterCfg config;
    
    protected final UUID id;
    
    protected final String application;
    
    protected final String info;
    
    protected final String hostName;
    
    protected final Consumer<Void> onPanic;
    
    protected final ZooKeeperManager zooKeeper;
    
    protected final HazelcastInstance hazelcast;
    
    protected final AtomicBoolean paniced = new AtomicBoolean(false);
    
    public BergamotMember(ClusterCfg config, Consumer<Void> onPanic, String application, String info) throws Exception
    {
        super();
        this.config = Objects.requireNonNull(config);
        this.onPanic = Objects.requireNonNull(onPanic);
        this.id = UUID.randomUUID();
        this.application = application;
        this.info = info;
        this.hostName = this.getHostName();
        // Connect
        logger.info("Connecting to Bergamot Cluster");
        this.zooKeeper = this.connectZooKeeper();
        this.zooKeeper.listen(this::zooKeeperListener);
        this.hazelcast = this.connectHazelcast();
        this.hazelcast.getLifecycleService().addLifecycleListener(this::hazelcastListener);
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
    
    protected String getHostName()
    {
        try
        {
            return Util.coalesceEmpty(System.getenv("BERGAMOT_HOSTNAME"), System.getProperty("bergamot.host.name"), InetAddress.getLocalHost().getHostName());
        }
        catch (UnknownHostException e)
        {
            logger.warn("Failed to get node host name", e);
        }
        return null;
    }
    
    protected HazelcastInstance connectHazelcast() throws Exception
    {
        logger.info("Connecting Hazelcast");
        Config config = this.configureHazelcast(HazelcastZooKeeperDiscovery.configureHazelcast(this.zooKeeper, new Config()));
        if (! Util.isEmpty(this.config.getPublicAddress()))
        {
            config.getNetworkConfig().setPublicAddress(this.config.getPublicAddress());
        }
        if (this.config.getPort() > 0)
        {
            config.getNetworkConfig().setPort(this.config.getPort());
        }
        return Hazelcast.newHazelcastInstance(config);
    }
    
    protected Config configureHazelcast(Config config)
    {
        // Set the class loader
        ClassLoader loader = this.getClass().getClassLoader();
        logger.info("Setting hazelcast class loader to: " + loader);
        config.setClassLoader(loader);
        // Configure the processors queue
        config.getRingbufferConfig(HZNames.buildProcessorRingbufferName(null))
            .setBackupCount(1)
            .setAsyncBackupCount(0)
            .setCapacity(5000)
            .setInMemoryFormat(InMemoryFormat.BINARY)
            .setTimeToLiveSeconds(300_000);
        config.getMapConfig(HZNames.buildProcessorsSequenceMapName())
            .setBackupCount(0)
            .setAsyncBackupCount(1)
            .setInMemoryFormat(InMemoryFormat.BINARY);
        // Configure the worker queues
        config.getRingbufferConfig(HZNames.buildWorkerRingbufferName(null))
            .setBackupCount(1)
            .setAsyncBackupCount(0)
            .setCapacity(1000)
            .setInMemoryFormat(InMemoryFormat.BINARY)
            .setTimeToLiveSeconds(300_000);
        config.getMapConfig(HZNames.buildWorkersSequenceMapName())
            .setBackupCount(0)
            .setAsyncBackupCount(1)
            .setInMemoryFormat(InMemoryFormat.BINARY);
        // Configure the notifier queues
        config.getRingbufferConfig(HZNames.buildNotifierRingbufferName(null))
            .setBackupCount(1)
            .setAsyncBackupCount(0)
            .setCapacity(5000)
            .setInMemoryFormat(InMemoryFormat.BINARY)
            .setTimeToLiveSeconds(300_000);
        config.getMapConfig(HZNames.buildNotifiersSequenceMapName())
            .setBackupCount(1)
            .setAsyncBackupCount(0)
            .setInMemoryFormat(InMemoryFormat.BINARY);
        // Configure site notification topic
        config.getReliableTopicConfig(HZNames.getSiteNotificationTopicName(null))
            .setTopicOverloadPolicy(TopicOverloadPolicy.DISCARD_OLDEST);
        // Configure site update topic
        config.getReliableTopicConfig(HZNames.getSiteNotificationTopicName(null))
            .setTopicOverloadPolicy(TopicOverloadPolicy.DISCARD_OLDEST);
        // Configure site event topic
        config.getReliableTopicConfig(HZNames.getSiteEventTopicName())
            .setTopicOverloadPolicy(TopicOverloadPolicy.DISCARD_OLDEST);
        // Configure scheduler topics
        config.getReliableTopicConfig(HZNames.getSchedulingTopicName())
            .setTopicOverloadPolicy(TopicOverloadPolicy.DISCARD_OLDEST);
        return config;
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
    
    protected void hazelcastListener(LifecycleEvent event)
    {
        if (event.getState() == LifecycleState.SHUTTING_DOWN || event.getState() == LifecycleState.SHUTDOWN)
        {
            this.panic();
        }
    }
    
    protected void panic()
    {
        if (this.paniced.compareAndSet(false, true))
        {
            logger.fatal("PANIC - lost connection to cluster");
            this.onPanic.accept(null);
        }
    }
    
    public boolean hasPaniced()
    {
        return this.paniced.get();
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
