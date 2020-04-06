package com.intrbiz.bergamot.cluster.registry;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
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
    
    protected final String containerPath;
    
    public GenericRegistar(ZooKeeper zooKeeper, String containerName) throws KeeperException, InterruptedException
    {
        super();
        this.zooKeeper = Objects.requireNonNull(zooKeeper);
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
    
    protected final void registerItem(K id, V data) throws KeeperException, InterruptedException
    {
        String path = this.zooKeeper.create(this.buildItemPath(id), this.transcoder.encodeAsBytes(data), Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.EPHEMERAL);
        logger.info("Registered into ZooKeeper: " + path);
    }
    
    protected final void reregisterItem(K id, V data) throws KeeperException, InterruptedException
    {
        this.zooKeeper.setData(this.buildItemPath(id), this.transcoder.encodeAsBytes(data), -1);
    }
    
    protected final void unregisterItem(K id) throws KeeperException, InterruptedException
    {
        this.zooKeeper.delete(this.buildItemPath(id), -1);
    }
    
    protected final String buildItemPath(K itemId)
    {
        return this.containerPath + "/" + itemId.toString();
    }
}
