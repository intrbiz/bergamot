package com.intrbiz.bergamot.cluster.registry;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.BadVersionException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NotEmptyException;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.io.BergamotTranscoder;

/**
 * Register items in and out of a registry.
 */
public abstract class GenericRegistar<K, V>
{
    private static final Logger logger = Logger.getLogger(GenericRegistar.class);
    
    protected final ZooKeeper zooKeeper;
    
    protected final BergamotTranscoder transcoder = BergamotTranscoder.getDefaultInstance();
    
    protected final Class<V> dataType;
    
    protected final String containerPath;
    
    public GenericRegistar(ZooKeeper zooKeeper, Class<V> dataType, String containerName) throws KeeperException, InterruptedException
    {
        super();
        this.zooKeeper = Objects.requireNonNull(zooKeeper);
        this.dataType = Objects.requireNonNull(dataType);
        this.containerPath = ZKPaths.BERGAMOT + "/" + Objects.requireNonNull(containerName);
        this.waitForContainer();
    }
    
    private void waitForContainer() throws KeeperException, InterruptedException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        Stat stat = this.zooKeeper.exists(this.containerPath, (watchedEvent) -> {
            if (watchedEvent.getType() == EventType.NodeCreated)
            {
                latch.countDown();
            }
        });
        // if the node doesn't exist wait for it to be created
        if (stat == null)
        {
            latch.await();
        }
    }
    
    protected final int registerItem(K id, V data) throws KeeperException, InterruptedException
    {
        Stat stat = new Stat();
        String path = this.zooKeeper.create(this.buildItemPath(id), this.transcoder.encodeAsBytes(data), Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.EPHEMERAL, stat);
        logger.info("Registered into ZooKeeper: " + path);
        return stat.getVersion();
    }
    
    protected final void unregisterItem(K id, int version) throws KeeperException, InterruptedException
    {
        String path = this.buildItemPath(id);
        logger.info("Unregistered from ZooKeeper: " + path + ", version=" + version);
        this.zooKeeper.delete(path, version);
    }
    
    protected final void unregisterItem(K id) throws KeeperException, InterruptedException
    {
        this.unregisterItem(id, -1);
    }
    
    public void unregisterItem(K id, Predicate<V> check) throws KeeperException, InterruptedException
    {
        // Ensure we only delete the node if the check passes
        try
        {
            String path = this.buildItemPath(id);
            logger.info("Attempting to unregister from ZooKeeper: " + path);
            // Get the current registration data
            Stat stat = new Stat();
            byte[] data = this.zooKeeper.getData(path, false, stat);
            if (data != null)
            {
                V reg = this.transcoder.decodeFromBytes(data, this.dataType);
                // Do we own this registration
                if (check.test(reg))
                {
                    // Delete the node
                    logger.info("Unregistering from ZooKeeper: " + path + " version=" + stat.getVersion());
                    this.zooKeeper.delete(path, stat.getVersion());
                }
            }
        }
        catch (NoNodeException | BadVersionException | NotEmptyException e)
        {
            // ignore
        }
    }
    
    protected final String buildItemPath(K itemId)
    {
        return this.containerPath + "/" + itemId.toString();
    }
}
