package com.intrbiz.bergamot.cluster.client.hz;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleEvent.LifecycleState;
import com.intrbiz.bergamot.cluster.client.BergamotClient;
import com.intrbiz.bergamot.cluster.discovery.HazelcastZooKeeperDiscovery;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager.ZooKeeperState;
import com.intrbiz.bergamot.util.HostUtil;

/**
 * A Bergamot client daemon which connects to the cluster and provides services
 */
public abstract class HZBergamotClient implements BergamotClient
{
    private static final Logger logger = Logger.getLogger(HZBergamotClient.class);
    
    protected final UUID id;
    
    protected final String hostName;
    
    protected final Consumer<Void> onPanic;
    
    protected final ZooKeeperManager zooKeeper;
    
    protected final HazelcastInstance hazelcast;
    
    protected final AtomicBoolean paniced = new AtomicBoolean(false);
    
    public HZBergamotClient(Consumer<Void> onPanic) throws Exception
    {
        super();
        this.onPanic = Objects.requireNonNull(onPanic);
        this.id = UUID.randomUUID();
        this.hostName = this.getHostName();
        // Connect
        logger.info("Connecting to Bergamot Cluster");
        this.zooKeeper = this.connectZooKeeper();
        this.zooKeeper.listen(this::zooKeeperListener);
        this.hazelcast = this.connectHazelcast();
        this.hazelcast.getLifecycleService().addLifecycleListener(this::hazelcastListener);
        logger.info("Connected to Bergamot Cluster");
    }
    
    protected String getHostName()
    {
        return HostUtil.getHostName();
    }
    
    protected HazelcastInstance connectHazelcast() throws Exception
    {
        logger.info("Connecting Hazelcast");
        ClientConfig cliCfg = this.configureHazelcast(HazelcastZooKeeperDiscovery.configureHazelcastClient(this.zooKeeper, new ClientConfig()));
        return HazelcastClient.newHazelcastClient(cliCfg); 
    }
    
    protected ClientConfig configureHazelcast(ClientConfig config)
    {
        ClassLoader loader = this.getClass().getClassLoader();
        logger.info("Setting hazelcast class loader to: " + loader);
        config.setClassLoader(loader);
        return config;
    }
    
    protected ZooKeeperManager connectZooKeeper() throws Exception
    {
        logger.info("Connecting ZooKeeper");
        return new ZooKeeperManager();
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
        this.paniced.set(true);
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
