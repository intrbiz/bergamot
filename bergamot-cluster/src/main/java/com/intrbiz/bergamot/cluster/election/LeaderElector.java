package com.intrbiz.bergamot.cluster.election;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.election.model.ElectionEvent;
import com.intrbiz.bergamot.cluster.election.model.ElectionEvent.Type;
import com.intrbiz.bergamot.cluster.util.ZKPaths;

/**
 * Elect a leader amongst the processors which execute specific coordination tasks for the whole cluster
 */
public final class LeaderElector extends GenericElector
{   
    private static final Logger logger = Logger.getLogger(LeaderElector.class);
    
    protected final ConcurrentMap<UUID, Consumer<ElectionEvent>> listeners = new ConcurrentHashMap<>();
    
    public LeaderElector(ZooKeeper zooKeeper, UUID id) throws KeeperException, InterruptedException
    {
        super(zooKeeper, ZKPaths.POOLS, ZKPaths.LEADER, id);
        // Watch for things to join the election
        this.setupWatcher();
    }
    
    private void setupWatcher() throws KeeperException, InterruptedException
    {
        this.zooKeeper.addWatch(this.containerPath,  (watchedEvent) -> {
            try
            {
                switch (watchedEvent.getType())
                {
                    case NodeCreated:
                        {
                            this.fireEvent(new ElectionEvent(Type.ADDED, UUID.fromString(watchedEvent.getPath().split("_")[0])));
                        }
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e)
            {
                logger.warn("Error processing ZooKeeper event for " + this.containerPath, e);
            }
        }, AddWatchMode.PERSISTENT);
    }
    
    protected void fireEvent(ElectionEvent event)
    {
        for (Consumer<ElectionEvent> listener : this.listeners.values())
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
     * Listen to election events
     * @param listener the listener to be invoked when an event happens
     * @return the listener id
     */
    public final UUID listen(Consumer<ElectionEvent> listener)
    {
        Objects.requireNonNull(listener);
        UUID id = UUID.randomUUID();
        this.listeners.put(id, listener);
        return id;
    }
    
    /**
     * Unlisten to election event
     * @param listenerId the id of listener to remove
     */
    public final void unlisten(UUID listenerId)
    {
        this.listeners.remove(listenerId);
    }
    
    public String toString()
    {
        return "leader";
    }
}
