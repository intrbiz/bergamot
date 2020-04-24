package com.intrbiz.bergamot.cluster.registry;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import com.intrbiz.bergamot.cluster.registry.model.RegistryEvent;
import com.intrbiz.bergamot.cluster.registry.model.RegistryEvent.Type;
import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.io.BergamotTranscoder;

/**
 * A generic registry of things
 */
public abstract class GenericRegistry<K, V>
{
    public static final Logger logger = Logger.getLogger(GenericRegistry.class);
    
    protected final ZooKeeper zooKeeper;
    
    protected final BergamotTranscoder transcoder = BergamotTranscoder.getDefaultInstance();
    
    protected final Class<V> dataType;
    
    protected final Function<String, K> idFromString;
    
    protected final String containerPath;
    
    protected final int itemPrefixLength;
    
    protected final ConcurrentMap<UUID, Consumer<RegistryEvent<K, V>>> listeners = new ConcurrentHashMap<>();
    
    public GenericRegistry(ZooKeeper zooKeeper, Class<V> dataType, Function<String, K> idFromString, String containerName) throws KeeperException, InterruptedException
    {
        super();
        this.zooKeeper = Objects.requireNonNull(zooKeeper);
        this.dataType = Objects.requireNonNull(dataType);
        this.idFromString = Objects.requireNonNull(idFromString);
        this.containerPath = ZKPaths.BERGAMOT + "/" + Objects.requireNonNull(containerName);
        this.itemPrefixLength = this.containerPath.length() + 1;
        // Ensure registration nodes are there
        this.createRoot();
        this.createContainer();
        // Watch for workers to register
        this.setupWatcher();
    }
    
    private void createRoot() throws KeeperException, InterruptedException
    {
        if (this.zooKeeper.exists(ZKPaths.BERGAMOT, false) == null)
        {
            this.zooKeeper.create(ZKPaths.BERGAMOT, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
        }
    }
    
    private void createContainer() throws KeeperException, InterruptedException
    {
        if (this.zooKeeper.exists(this.containerPath, false) == null)
        {
            this.zooKeeper.create(this.containerPath, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
        }
    }
    
    private void setupWatcher() throws KeeperException, InterruptedException
    {
        this.zooKeeper.addWatch(this.containerPath,  (watchedEvent) -> {
            logger.info("Processing registry event for " + this.containerPath + ": " + watchedEvent);
            try
            {
                switch (watchedEvent.getType())
                {
                    case NodeCreated:
                        {
                            K itemId = this.getItemIdFromPath(watchedEvent.getPath());
                            V item = this.getItem(watchedEvent.getPath());
                            this.onItemAdded(itemId, item);
                            this.fireEvent(new RegistryEvent<>(Type.ADDED, itemId, item));
                        }
                        break;
                    case NodeDeleted:
                        {
                            K itemId = this.getItemIdFromPath(watchedEvent.getPath());
                            this.onItemRemoved(itemId);
                            this.fireEvent(new RegistryEvent<>(Type.REMOVED, itemId, null));
                        }
                        break;
                    case NodeDataChanged:
                        {
                            K itemId = this.getItemIdFromPath(watchedEvent.getPath());
                            V item = this.getItem(watchedEvent.getPath());
                            this.onItemUpdated(itemId, item);
                            this.fireEvent(new RegistryEvent<>(Type.UPDATED, itemId, item));
                        }
                    case None:
                        {
                            if (watchedEvent.getState() == KeeperState.SyncConnected)
                            {
                                logger.info("Reconnected to ZooKeeper, recreating watch");
                                this.setupWatcher();
                                this.onConnect();
                                this.fireEvent(new RegistryEvent<>(Type.CONNECTED, null, null));
                            }
                            else if (watchedEvent.getState() == KeeperState.Disconnected)
                            {
                                logger.warn("Lost connection to ZooKeeper, waiting for reconnect");
                                this.onDisconnect();
                                this.fireEvent(new RegistryEvent<>(Type.DISCONNECTED, null, null));
                            }
                        }
                        break;
                    default:
                        logger.warn("Ignoring event on " + this.containerPath + ": " + watchedEvent);
                }
            }
            catch (Exception e)
            {
                logger.warn("Error processing ZooKeeper event for " + this.containerPath, e);
            }
        }, AddWatchMode.PERSISTENT_RECURSIVE);
    }
    
    protected void fireEvent(RegistryEvent<K, V> event)
    {
        for (Consumer<RegistryEvent<K, V>> listener : this.listeners.values())
        {
            try
            {
                listener.accept(event);
            }
            catch (Exception e)
            {
                logger.warn("Listener error whilst processing event for " + this.containerPath, e);
            }
        }
    }
    
    /**
     * Listen to Registry events
     * @param listener the listener to be invoked when an event happens
     * @return the listener id
     */
    public final UUID listen(Consumer<RegistryEvent<K, V>> listener)
    {
        Objects.requireNonNull(listener);
        UUID id = UUID.randomUUID();
        this.listeners.put(id, listener);
        return id;
    }
    
    /**
     * Unlisten to Registry event
     * @param listenerId the id of listener to remove
     */
    public final void unlisten(UUID listenerId)
    {
        this.listeners.remove(listenerId);
    }
    
    public String getContainerPath()
    {
        return this.containerPath;
    }
    
    public int count() throws KeeperException, InterruptedException
    {
        return this.zooKeeper.getChildren(this.containerPath, false).size();
    }
    
    /**
     * Get the registration data for a specific item from ZooKeeper
     * @param itemId the item id
     * @return the item registration data or null
     * @throws KeeperException
     * @throws InterruptedException
     */
    protected final V getItem(K itemId) throws KeeperException, InterruptedException
    {
        return this.getItem(this.buildItemPath(itemId));
    }
    
    /**
     * Get the current list of items from ZooKeeper 
     * @return The list of currently registered items
     * @throws KeeperException
     * @throws InterruptedException
     */
    protected final Set<V> getItems() throws KeeperException, InterruptedException
    {
        Set<V> workers = new TreeSet<>();
        for (String path : this.zooKeeper.getChildren(this.containerPath, false))
        {
            workers.add(this.getItem(this.buildItemPath(path)));
        }
        return workers;
    }
    
    protected final V getItem(String path) throws KeeperException, InterruptedException
    {
        try
        {
            byte[] workerData = this.zooKeeper.getData(path, false, null);
            if (workerData != null)
            {
                return this.transcoder.decodeFromBytes(workerData, this.dataType);
            }
        }
        catch (NoNodeException nn)
        {
            // ignore no node errors
        }
        return null;
    }
    
    protected final String buildItemPath(String itemId)
    {
        return this.containerPath + "/" + itemId;
    }
    
    protected final String buildItemPath(K item)
    {
        return buildItemPath(item.toString());
    }
    
    protected final K getItemIdFromPath(String itemFullPath)
    {
        return this.idFromString.apply(itemFullPath.substring(this.itemPrefixLength));
    }
    
    // Hooks
    
    protected void onItemAdded(K id, V item)
    {
    }
    
    protected void onItemRemoved(K id)
    {
    }
    
    protected void onItemUpdated(K id, V item)
    {
    }
    
    protected void onConnect()
    {
    }
    
    protected void onDisconnect()
    {
    }
}
