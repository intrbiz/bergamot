package com.intrbiz.bergamot.cluster.zookeeper;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.intrbiz.bergamot.BergamotConfig;

import org.apache.zookeeper.ZooKeeper;

public final class ZooKeeperManager
{
    public enum ZooKeeperState { CONNECTED, DISCONNECTED, EXPIRED }
    
    public static final int SESSION_TIMEOUT = 15_000;
    
    public static final int INITIAL_CONNECT_TIMEOUT = 60;
    
    private static final Logger logger = Logger.getLogger(ZooKeeperManager.class);
    
    private final String connectString;
    
    private final ZooKeeper zooKeeper;
    
    private final Object connectLock = new Object();
    
    private final ConcurrentMap<UUID, Consumer<ZooKeeperState>> listeners = new ConcurrentHashMap<>();
    
    public ZooKeeperManager() throws IOException
    {
        this.connectString = Objects.requireNonNull(BergamotConfig.getZooKeeperNodes(), "The ZooKeeper nodes must be given");
        // Connect to ZooKeeper
        this.zooKeeper = new ZooKeeper(this.connectString, SESSION_TIMEOUT, this::processWatchedEvent);
        this.waitForInitialConnect();
    }
    
    private void waitForInitialConnect()
    {
        for (int i = 0; i < 60; i++)
        {
            if (this.zooKeeper.getState().isConnected() && this.zooKeeper.getState().isAlive())
            {
                logger.info("Connected to ZooKeeper");
                return;
            }
            try
            {
                synchronized (this.connectLock)
                {
                    this.connectLock.wait(1_000);
                }
            }
            catch (InterruptedException ie)
            {
            }
        }
        throw new RuntimeException("Failed to connect to ZooKeeper");
    }
    
    private void processWatchedEvent(WatchedEvent event)
    {
        logger.info("ZooKeeper event: " + event);
        if (event.getType() == EventType.None)
        {
            if (event.getState() == KeeperState.SyncConnected)
            {
                // Notify anything waiting on the connect lock when we get a connect event.
                synchronized (this.connectLock)
                {
                    this.connectLock.notifyAll();
                }
                this.fireEvent(ZooKeeperState.CONNECTED);
            }
            else if (event.getState() == KeeperState.Disconnected)
            {
                this.fireEvent(ZooKeeperState.DISCONNECTED);
            }
            else if (event.getState() == KeeperState.Expired)
            {
                this.fireEvent(ZooKeeperState.EXPIRED);
            }
        }
    }
    
    protected void fireEvent(ZooKeeperState state)
    {
        for (Consumer<ZooKeeperState> listener : this.listeners.values())
        {
            try
            {
                listener.accept(state);
            }
            catch (Exception e)
            {
                logger.warn("A listener threw an error whilst processing an event", e);
            }
        }
    }
    
    /**
     * Listen to state changes in the ZooKeeper connection state
     * @param listener the listener to add
     * @return the id of the added listener
     */
    public UUID listen(Consumer<ZooKeeperState> listener)
    {
        UUID id = UUID.randomUUID();
        this.listeners.put(id, listener);
        return id;
    }
    
    /**
     * Remove the given listener
     * @param id the listener id
     */
    public void unlisten(UUID id)
    {
        this.listeners.remove(id);
    }
    
    public boolean isConnected()
    {
        return this.zooKeeper.getState().isConnected();
    }
    
    public boolean isAlive()
    {
        return this.zooKeeper.getState().isAlive();
    }
    
    public ZooKeeper getZooKeeper()
    {
        return this.zooKeeper;
    }
    
    public void shutdown()
    {
        try
        {
            this.zooKeeper.close();
        }
        catch (InterruptedException e)
        {
            logger.error("Failed to shutdown ZooKeeper", e);
        }
    }
}
