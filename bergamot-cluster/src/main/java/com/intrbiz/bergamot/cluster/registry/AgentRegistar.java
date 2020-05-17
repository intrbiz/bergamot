package com.intrbiz.bergamot.cluster.registry;

import java.util.UUID;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.BadVersionException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.KeeperException.NotEmptyException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

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
    
    public void unregisterAgent(UUID agentId, UUID workerId) throws KeeperException, InterruptedException
    {
        // Ensure we only delete the node if it is for this worker
        try
        {
            String path = this.buildItemPath(agentId);
            // Get the current registration data
            Stat stat = new Stat();
            byte[] agentData = this.zooKeeper.getData(path, false, stat);
            if (agentData != null)
            {
                AgentRegistration reg = this.transcoder.decodeFromBytes(agentData, AgentRegistration.class);
                // Do we own this registration
                if (workerId.equals(reg.getWorkerId()))
                {
                    // Delete the node
                    this.zooKeeper.delete(path, stat.getVersion());
                }
            }
        }
        catch (NoNodeException | BadVersionException | NotEmptyException e)
        {
            // ignore
        }
    }
}
