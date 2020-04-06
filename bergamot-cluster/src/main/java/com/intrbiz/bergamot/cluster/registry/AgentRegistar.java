package com.intrbiz.bergamot.cluster.registry;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.ZooKeeper;

import com.intrbiz.bergamot.cluster.util.ZKPaths;
import com.intrbiz.bergamot.model.message.cluster.AgentRegistration;

/**
 * Register Agents in and out of the registry.
 */
public class AgentRegistar extends GenericRegistar<UUID, AgentRegistration>
{    
    public AgentRegistar(ZooKeeper zooKeeper) throws KeeperException, InterruptedException
    {
        super(zooKeeper, ZKPaths.AGENTS);
    }
    
    public void registerAgent(AgentRegistration agent) throws KeeperException, InterruptedException
    {
        try
        {
            this.registerItem(agent.getId(), agent);
        }
        catch (NodeExistsException e)
        {
            // update the registration instead
            this.reregisterItem(agent.getId(), agent);
        }
    }
    
    public void unregisterAgent(UUID agentId) throws KeeperException, InterruptedException
    {
        this.unregisterItem(agentId);
    }
}
