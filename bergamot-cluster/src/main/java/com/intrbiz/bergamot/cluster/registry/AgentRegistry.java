package com.intrbiz.bergamot.cluster.registry;

import java.util.Objects;
import java.util.Set;
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
public class AgentRegistry  extends GenericNamespacedRegistry<UUID, UUID, AgentRegistration>
{
    public static final Logger logger = Logger.getLogger(AgentRegistry.class);
    
    private final ConcurrentMap<AgentCacheKey, UUID> agentsCache = new ConcurrentHashMap<>();
    
    public AgentRegistry(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, AgentRegistration.class, UUID::fromString, UUID::fromString, ZKPaths.AGENTS);
    }
    
    @Override
    protected void onConnect()
    {
        logger.info("Clearing agent cache");
        this.agentsCache.clear();
    }
    
    @Override
    protected void onDisconnect()
    {
        logger.info("Clearing agent cache");
        this.agentsCache.clear();
    }
    
    @Override
    protected void onItemRemoved(UUID siteId, UUID id)
    {
        logger.info("Removing agent: " + id);
        this.agentsCache.remove(new AgentCacheKey(siteId, id));
    }

    @Override
    protected void onItemUpdated(UUID siteId, UUID id, AgentRegistration item)
    {
        logger.info("Updating agent: " + id);
        this.agentsCache.remove(new AgentCacheKey(siteId, id));
    }

    /**
     * Route the agent based check to the required worker.
     * @param siteId the site id
     * @param agentId the agent id
     * @return the worker id
     */
    public UUID routeAgent(UUID siteId, UUID agentId)
    {
        // TODO: our caching approach will cause lots of ZK lookups when agents are 
        // disconnected, we might need to optimise that later.
        return this.agentsCache.computeIfAbsent(new AgentCacheKey(siteId, agentId), (key) -> {
            try
            {
                AgentRegistration agentReg = this.getAgent(key.siteId, key.agentId);
                if (agentReg != null) 
                    return agentReg.getWorkerId();
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
    public AgentRegistration getAgent(UUID siteId, UUID agentId) throws KeeperException, InterruptedException
    {
        return this.getItem(siteId, agentId);
    }
    
    public Set<UUID> getSites() throws KeeperException, InterruptedException
    {
        return this.getNamespaces();
    }
    
    public Set<AgentRegistration> getAgentsForSite(UUID siteId) throws KeeperException, InterruptedException
    {
        return this.getItems(Objects.requireNonNull(siteId));
    }
    
    private static final class AgentCacheKey implements Comparable<AgentCacheKey>
    {
        public final UUID siteId;
        
        public final UUID agentId;
        
        public AgentCacheKey(UUID siteId, UUID agentId)
        {
            this.siteId = siteId;
            this.agentId = agentId;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
            result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            AgentCacheKey other = (AgentCacheKey) obj;
            if (agentId == null)
            {
                if (other.agentId != null) return false;
            }
            else if (!agentId.equals(other.agentId)) return false;
            if (siteId == null)
            {
                if (other.siteId != null) return false;
            }
            else if (!siteId.equals(other.siteId)) return false;
            return true;
        }

        @Override
        public int compareTo(AgentCacheKey o)
        {
            return this.siteId.equals(o.siteId) ? this.agentId.compareTo(o.agentId) : this.siteId.compareTo(o.siteId);
        }
    }
}
