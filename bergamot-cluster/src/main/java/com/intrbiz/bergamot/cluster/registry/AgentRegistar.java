package com.intrbiz.bergamot.cluster.registry;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.AgentRegistration;

/**
 * Register Agents in and out of the registry.
 */
public class AgentRegistar extends GenericNamespacedRegistar<UUID, UUID, AgentRegistration>
{
    public AgentRegistar(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, AgentRegistration.class, ZKPaths.AGENTS);
    }
    
    public int registerAgent(UUID siteId, AgentRegistration agent) throws KeeperException, InterruptedException
    {
        return this.registerItem(siteId, agent.getId(), agent, true);
    }
    
    public void unregisterAgent(UUID siteId, UUID agentId, UUID nonce, UUID workerId) throws KeeperException, InterruptedException
    {
        this.unregisterItem(siteId, agentId, reg -> nonce.equals(reg.getNonce()) && workerId.equals(reg.getWorkerId()));
    }
}
