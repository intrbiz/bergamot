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
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import com.intrbiz.bergamot.cluster.registry.model.NamespacedRegistryEvent;
import com.intrbiz.bergamot.cluster.registry.model.RegistryEvent.Type;
import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.io.BergamotCoreTranscoder;

/**
 * A generic namespaced registry of things
 */
public class GenericNamespacedRegistry<N, K, V>
{
    public static final Logger logger = Logger.getLogger(GenericRegistry.class);
    
    protected final ZooKeeper zooKeeper;
    
    protected final BergamotCoreTranscoder transcoder = BergamotCoreTranscoder.getDefault();
    
    protected final Class<V> dataType;
    
    protected final Function<String, N> namespaceFromString;
    
    protected final Function<String, K> idFromString;
    
    protected final String containerPath;
    
    protected final int itemPrefixLength;
    
    protected final ConcurrentMap<UUID, Consumer<NamespacedRegistryEvent<N, K, V>>> listeners = new ConcurrentHashMap<>();
    
    public GenericNamespacedRegistry(ZooKeeper zooKeeper, Class<V> dataType, Function<String, N> namespaceFromString, Function<String, K> idFromString, String containerName) throws KeeperException, InterruptedException
    {
        super();
        this.zooKeeper = Objects.requireNonNull(zooKeeper);
        this.dataType = Objects.requireNonNull(dataType);
        this.namespaceFromString = Objects.requireNonNull(namespaceFromString);
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
            try
            {
                this.zooKeeper.create(ZKPaths.BERGAMOT, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
            }
            catch (NodeExistsException e)
            {
                // ignore
            }
        }
    }
    
    private void createContainer() throws KeeperException, InterruptedException
    {
        if (this.zooKeeper.exists(this.containerPath, false) == null)
        {
            try
            {
                this.zooKeeper.create(this.containerPath, null, Arrays.asList(new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.ANYONE_ID_UNSAFE)), CreateMode.PERSISTENT);
            }
            catch (NodeExistsException e)
            {
                // ignore
            }
        }
    }
    
    private void setupWatcher() throws KeeperException, InterruptedException
    {
        this.zooKeeper.addWatch(this.containerPath,  (watchedEvent) -> {
            if (logger.isDebugEnabled()) logger.debug("Processing namespaced registry event for " + this.containerPath + ": " + watchedEvent);
            try
            {
                switch (watchedEvent.getType())
                {
                    case NodeCreated:
                        {
                            N namespace = this.getNamespaceFromPath(watchedEvent.getPath());
                            K itemId = this.getItemIdFromPath(watchedEvent.getPath());
                            if (namespace != null && itemId != null)
                            {
                                V item = this.getItem(watchedEvent.getPath());
                                this.onItemAdded(namespace, itemId, item);
                                this.fireEvent(new NamespacedRegistryEvent<>(Type.ADDED, namespace, itemId, item));
                            }
                        }
                        break;
                    case NodeDeleted:
                        {
                            N namespace = this.getNamespaceFromPath(watchedEvent.getPath());
                            K itemId = this.getItemIdFromPath(watchedEvent.getPath());
                            if (namespace != null && itemId != null)
                            {
                                this.onItemRemoved(namespace, itemId);
                                this.fireEvent(new NamespacedRegistryEvent<>(Type.REMOVED, namespace, itemId, null));
                            }
                        }
                        break;
                    case NodeDataChanged:
                        {
                            N namespace = this.getNamespaceFromPath(watchedEvent.getPath());
                            K itemId = this.getItemIdFromPath(watchedEvent.getPath());
                            if (namespace != null && itemId != null)
                            {
                                V item = this.getItem(watchedEvent.getPath());
                                this.onItemUpdated(namespace, itemId, item);
                                this.fireEvent(new NamespacedRegistryEvent<>(Type.UPDATED, namespace, itemId, item));
                            }
                        }
                    case None:
                        {
                            if (watchedEvent.getState() == KeeperState.SyncConnected)
                            {
                                logger.info("Reconnected to ZooKeeper, recreating watch");
                                this.setupWatcher();
                                this.onConnect();
                                this.fireEvent(new NamespacedRegistryEvent<>(Type.CONNECTED, null, null, null));
                            }
                            else if (watchedEvent.getState() == KeeperState.Disconnected)
                            {
                                logger.warn("Lost connection to ZooKeeper, waiting for reconnect");
                                this.onDisconnect();
                                this.fireEvent(new NamespacedRegistryEvent<>(Type.DISCONNECTED, null, null, null));
                            }
                        }
                        break;
                    default:
                        if (logger.isDebugEnabled()) logger.debug("Ignoring event on " + this.containerPath + ": " + watchedEvent);
                }
            }
            catch (Exception e)
            {
                logger.warn("Error processing ZooKeeper event for " + this.containerPath, e);
            }
        }, AddWatchMode.PERSISTENT_RECURSIVE);
    }
    
    protected void fireEvent(NamespacedRegistryEvent<N, K, V> event)
    {
        for (Consumer<NamespacedRegistryEvent<N, K, V>> listener : this.listeners.values())
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
    public final UUID listen(Consumer<NamespacedRegistryEvent<N, K, V>> listener)
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
    
    public int countNamespaces() throws KeeperException, InterruptedException
    {
        return this.zooKeeper.getChildren(this.containerPath, false).size();
    }
    
    public int count(N namespace) throws KeeperException, InterruptedException
    {
        return this.zooKeeper.getChildren(this.buildNamespacePath(namespace), false).size();
    }
    
    /**
     * Get the registration data for a specific item from ZooKeeper
     * @param namespace the namespace
     * @param itemId the item id
     * @return the item registration data or null
     * @throws KeeperException
     * @throws InterruptedException
     */
    protected final V getItem(N namespace, K itemId) throws KeeperException, InterruptedException
    {
        return this.getItem(this.buildItemPath(namespace, itemId));
    }
    
    /**
     * Get the current list of items from ZooKeeper for the given namespace
     * @return The list of currently registered items
     * @throws KeeperException
     * @throws InterruptedException
     */
    protected final Set<N> getNamespaces() throws KeeperException, InterruptedException
    {
        Set<N> namespaces = new TreeSet<>();
        for (String path : this.zooKeeper.getChildren(this.containerPath, false))
        {
            namespaces.add(this.namespaceFromString.apply(path));
        }
        return namespaces;
    }
    
    /**
     * Get the current list of items from ZooKeeper for the given namespace
     * @param namespace the id of the namespace to list
     * @return The list of currently registered items
     * @throws KeeperException
     * @throws InterruptedException
     */
    protected final Set<V> getItems(N namespace) throws KeeperException, InterruptedException
    {
        Set<V> items = new TreeSet<>();
        try
        {
            for (String path : this.zooKeeper.getChildren(this.buildNamespacePath(namespace), false))
            {
                V item = this.getItem(this.buildItemPath(namespace, path));
                if (item != null) items.add(item);
            }
        }
        catch (NoNodeException e)
        {
            // ignore no node errors
        }
        return items;
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
    
    protected final String buildNamespacePath(String namespaceId)
    {
        return this.containerPath + "/" + namespaceId;
    }
    
    protected final String buildNamespacePath(N namespace)
    {
        return this.buildNamespacePath(namespace.toString());
    }
    
    protected final String buildItemPath(String namespaceId, String itemId)
    {
        return this.buildNamespacePath(namespaceId) + "/" + itemId;
    }
    
    protected final String buildItemPath(N namespace, K item)
    {
        return buildItemPath(namespace.toString(), item.toString());
    }
    
    protected final String buildItemPath(N namespace, String itemId)
    {
        return buildItemPath(namespace.toString(), itemId);
    }
    
    protected final K getItemIdFromPath(String itemFullPath)
    {
        int start = itemFullPath.lastIndexOf('/');
        return start > this.itemPrefixLength ? this.idFromString.apply(itemFullPath.substring(start + 1)) : null;
    }
    
    protected final N getNamespaceFromPath(String itemFullPath)
    {
        int end = itemFullPath.lastIndexOf('/');
        return end > this.itemPrefixLength ? this.namespaceFromString.apply(itemFullPath.substring(this.itemPrefixLength, end)) : null;
    }
    
    // Hooks
    
    protected void onItemAdded(N namespace, K id, V item)
    {
    }
    
    protected void onItemRemoved(N namespace, K id)
    {
    }
    
    protected void onItemUpdated(N namespace, K id, V item)
    {
    }
    
    protected void onConnect()
    {
    }
    
    protected void onDisconnect()
    {
    }
}
