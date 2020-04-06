package com.intrbiz.bergamot.cluster.discovery;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.Address;
import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.hazelcast.spi.partitiongroup.PartitionGroupStrategy;
import com.intrbiz.Util;
import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.cluster.zookeeper.ZooKeeperManager;
import com.intrbiz.bergamot.io.BergamotTranscoder;
import com.intrbiz.bergamot.model.message.cluster.NodeRegistration;

/**
 * Discovery of Hazelcast members via ZooKeeper
 */
public class HazelcastZooKeeperDiscovery implements DiscoveryStrategy
{
    private static final Logger logger = Logger.getLogger(HazelcastZooKeeperDiscovery.class);

    protected final ZooKeeper zooKeeper;
    
    protected final BergamotTranscoder transcoder = BergamotTranscoder.getDefaultInstance();
    
    protected final DiscoveryNode thisNode;
    
    protected String registeredPath;
    
    public HazelcastZooKeeperDiscovery(ZooKeeper zooKeeper, DiscoveryNode thisNode)
    {
        super();
        this.zooKeeper = zooKeeper;
        this.thisNode = thisNode;
    }
    
    protected boolean isMember()
    {
        return this.thisNode != null;
    }

    @Override
    public void start()
    {
        try
        {
            this.createRoot();
            this.createContainer();
            if (this.isMember())
            {
                this.registerMember(this.thisNode);
            }
        }
        catch (KeeperException | InterruptedException e)
        {
            throw new IllegalStateException("Failed to start ZooKeeper discover", e);
        }
    }
    
    protected void createRoot() throws KeeperException, InterruptedException
    {
        if (this.zooKeeper.exists(ZKPaths.BERGAMOT, false) == null)
        {
            this.zooKeeper.create(ZKPaths.BERGAMOT, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
        }
    }
    
    protected void createContainer() throws KeeperException, InterruptedException
    {
        if (this.zooKeeper.exists(ZKPaths.BERGAMOT + "/" + ZKPaths.NODES, false) == null)
        {
            this.zooKeeper.create(ZKPaths.BERGAMOT + "/" + ZKPaths.NODES, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
        }
    }
    
    protected final void registerMember(DiscoveryNode node) throws KeeperException, InterruptedException
    {
        NodeRegistration data = new NodeRegistration();
        data.setPrivateAddress(node.getPrivateAddress().getHost());
        data.setPrivatePort(node.getPrivateAddress().getPort());
        if (node.getPublicAddress() != null)
        {
            data.setPublicAddress(node.getPublicAddress().getHost());
            data.setPublicPort(node.getPublicAddress().getPort());
        }
        this.registeredPath = this.zooKeeper.create(ZKPaths.BERGAMOT + "/" + ZKPaths.NODES + "/node_", this.transcoder.encodeAsBytes(data), Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.EPHEMERAL_SEQUENTIAL);
        logger.info("Registered member into ZooKeeper: " + this.registeredPath + " => " + data);
    }
    
    protected final void unregisterMember() throws KeeperException, InterruptedException
    {
        this.zooKeeper.delete(this.registeredPath, -1);
        logger.info("Unregistered member form ZooKeeper: " + this.registeredPath);
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes()
    {
        List<DiscoveryNode> nodes = new ArrayList<>();
        try
        {
            for (String child : this.zooKeeper.getChildren(ZKPaths.BERGAMOT + "/" + ZKPaths.NODES, false))
            {
                NodeRegistration node = this.getNode(ZKPaths.BERGAMOT + "/" + ZKPaths.NODES + "/" + child);
                if (node != null)
                {
                    nodes.add(new SimpleDiscoveryNode(
                        new Address(node.getPrivateAddress(), node.getPrivatePort()), 
                        Util.isEmpty(node.getPublicAddress()) ? null : new Address(node.getPublicAddress(), node.getPublicPort())
                    ));
                }
            }
        }
        catch (KeeperException | InterruptedException | UnknownHostException e)
        {
            throw new IllegalStateException("Failed to discover nodes in ZooKeeper", e);
        }
        return nodes;
    }
    
    private final NodeRegistration getNode(String path) throws KeeperException, InterruptedException
    {
        try
        {
            byte[] workerData = this.zooKeeper.getData(path, false, null);
            if (workerData != null)
            {
                return this.transcoder.decodeFromBytes(workerData, NodeRegistration.class);
            }
        }
        catch (NoNodeException nn)
        {
            // ignore no node errors
        }
        return null;
    }

    @Override
    public void destroy()
    {
        try
        {
            if (this.isMember() && this.registeredPath != null)
            {
                this.unregisterMember();
            }
        }
        catch (KeeperException | InterruptedException e)
        {
            logger.warn("Failed to unregister node from ZooKeeper", e);
        }
    }

    @Override
    public PartitionGroupStrategy getPartitionGroupStrategy()
    {
        return null;
    }

    @Override
    public Map<String, String> discoverLocalMetadata()
    {
        return Collections.emptyMap();
    }
    
    public static final class Factory implements DiscoveryStrategyFactory
    {
        private final ZooKeeperManager zooKeeper;
        
        public Factory(ZooKeeperManager zooKeeper)
        {
            super();
            this.zooKeeper = zooKeeper;
        }

        @Override
        public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType()
        {
            return HazelcastZooKeeperDiscovery.class;
        }

        @Override
        public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger, @SuppressWarnings("rawtypes") Map<String, Comparable> properties)
        {
            return new HazelcastZooKeeperDiscovery(this.zooKeeper.getZooKeeper(), discoveryNode);
        }

        @Override
        public Collection<PropertyDefinition> getConfigurationProperties()
        {
            return Collections.emptyList();
        }
    }
    
    public static Config configureHazelcast(ZooKeeperManager zooKeeper, Config config)
    {
        config.setProperty("hazelcast.discovery.enabled", "true");
        config.setProperty("hazelcast.discovery.public.ip.enabled", "true");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAzureConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getGcpConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getEurekaConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getDiscoveryConfig().addDiscoveryStrategyConfig(new DiscoveryStrategyConfig(new Factory(zooKeeper)));
        return config;
    }
    
    public static ClientConfig configureHazelcastClient(ZooKeeperManager zooKeeper, ClientConfig config)
    {
        config.setProperty("hazelcast.discovery.enabled", "true");
        config.setProperty("hazelcast.discovery.public.ip.enabled", "true");
        config.getNetworkConfig().getAwsConfig().setEnabled(false);
        config.getNetworkConfig().getAzureConfig().setEnabled(false);
        config.getNetworkConfig().getGcpConfig().setEnabled(false);
        config.getNetworkConfig().getKubernetesConfig().setEnabled(false);
        config.getNetworkConfig().getEurekaConfig().setEnabled(false);
        config.getNetworkConfig().getCloudConfig().setEnabled(false);
        config.getNetworkConfig().getDiscoveryConfig().addDiscoveryStrategyConfig(new DiscoveryStrategyConfig(new Factory(zooKeeper)));
        return config;
    }
}
