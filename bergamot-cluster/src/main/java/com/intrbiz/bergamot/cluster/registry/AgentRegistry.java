package com.intrbiz.bergamot.cluster.registry;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.AgentRegistration;

/**
 * A registry of Bergamot Workers
 */
public class AgentRegistry  extends GenericRegistry<UUID, AgentRegistration>
{
    public static final Logger logger = Logger.getLogger(AgentRegistry.class);
    
    private final ConcurrentMap<UUID, UUID> agentsCache = new ConcurrentHashMap<>();
    
    public AgentRegistry(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, AgentRegistration.class, UUID::fromString, ZKPaths.AGENTS);
    }
    
    @Override
    protected void onItemRemoved(UUID id)
    {
        this.agentsCache.remove(id);
    }

    @Override
    protected void onItemUpdated(UUID id, AgentRegistration item)
    {
        // if we already have the agent cached, update it as it is likely hot on this node
        this.agentsCache.computeIfPresent(id, (k, v) -> item.getWorkerId());
    }

    /**
     * Route the agent based check to the required worker.
     * @param agentId the agent id
     * @return the worker id
     */
    public UUID routeAgent(UUID agentId)
    {
        // TODO: our caching approach will cause lots of ZK lookups when agents are 
        // disconnected, we might need to optimise that later.
        return this.agentsCache.computeIfAbsent(agentId, (key) -> {
            try
            {
                AgentRegistration agentReg = this.getAgent(key);
                if (agentReg != null) agentReg.getWorkerId();
            }
            catch (KeeperException | InterruptedException e)
            {
                logger.warn("Error looking up agent registration", e);
            }
            return null;
        });
    }

    /**
     * Get the registration data for a specific Agent from ZooKeeper
     * @param agentId the agent id
     * @return the Agent registration data or null
     * @throws KeeperException
     * @throws InterruptedException
     */
    public AgentRegistration getAgent(UUID agentId) throws KeeperException, InterruptedException
    {
        return this.getItem(agentId);
    }
}
