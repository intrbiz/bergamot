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
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.KeeperException.NotEmptyException;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.io.BergamotTranscoder;

/**
 * Register items in and out of a namespaced registry.
 */
public abstract class GenericNamespacedRegistar<N, K, V>
{
    private static final Logger logger = Logger.getLogger(GenericNamespacedRegistar.class);
    
    protected final ZooKeeper zooKeeper;
    
    protected final BergamotTranscoder transcoder = BergamotTranscoder.getDefaultInstance();
    
    protected final Class<V> dataType;
    
    protected final String containerPath;
    
    public GenericNamespacedRegistar(ZooKeeper zooKeeper, Class<V> dataType, String containerName) throws KeeperException, InterruptedException
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
    
    protected void createNamespace(N namespace) throws KeeperException, InterruptedException
    {
        String path = this.buildNamespacePath(namespace);
        if (this.zooKeeper.exists(path, false) == null)
        {
            try
            {
                this.zooKeeper.create(path, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
            }
            catch (NodeExistsException e)
            {
                // ignore
            }
        }
    }
    
    protected final int registerItem(N namespace, K id, V data, boolean overwrite) throws KeeperException, InterruptedException
    {
        try
        {
            return this.registerItem(namespace, id, data);
        }
        catch (NodeExistsException e)
        {
            if (overwrite)
            {
                // forcefully delete
                this.unregisterItem(namespace, id);
                // register again
                return this.registerItem(namespace, id, data);
            }
            else
            {
                throw e;
            }
        }
    }
    
    protected final int registerItem(N namespace, K id, V data) throws KeeperException, InterruptedException
    {
        this.createNamespace(namespace);
        Stat stat = new Stat();
        String path = this.zooKeeper.create(this.buildItemPath(namespace, id), this.transcoder.encodeAsBytes(data), Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.EPHEMERAL, stat);
        logger.info("Registered into ZooKeeper: " + path);
        return stat.getVersion();
    }
    
    protected final void unregisterItem(N namespace, K id, int version) throws KeeperException, InterruptedException
    {
        try
        {
            String path = this.buildItemPath(namespace, id);
            logger.info("Unregistered from ZooKeeper: " + path + ", version=" + version);
            this.zooKeeper.delete(path, version);
        }
        catch (NoNodeException e)
        {
            // ignore
        }
    }
    
    protected final void unregisterItem(N namespace, K id) throws KeeperException, InterruptedException
    {
        this.unregisterItem(namespace, id, -1);
    }
    
    public void unregisterItem(N namespace, K id, Predicate<V> check) throws KeeperException, InterruptedException
    {
        // Ensure we only delete the node if it is for this worker
        try
        {
            String path = this.buildItemPath(namespace, id);
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
    
    protected final String buildNamespacePath(N namespace)
    {
        return this.containerPath + "/" + namespace.toString();
    }
    
    protected final String buildItemPath(N namespace, K itemId)
    {
        return this.buildNamespacePath(namespace) + "/" + itemId.toString();
    }
}
