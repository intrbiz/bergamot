package com.intrbiz.bergamot.cluster.client;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.PoolDispatcher;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyClientLookup;
import com.intrbiz.bergamot.cluster.registry.AgentRegistar;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistar;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.model.message.cluster.AgentRegistration;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * A Bergamot Worker node client
 */
public class WorkerClient extends BergamotClient
{   
    private final WorkerRegistar registar;
    
    private final WorkerConsumer consumer;
    
    private final PoolDispatcher dispatcher;
    
    private final AgentKeyClientLookup agentKeyLookup;
    
    private final AgentRegistar agentRegistar;

    public WorkerClient(ClusterCfg config, Consumer<Void> onPanic, String application, String info, String hostName) throws Exception
    {
        super(config, onPanic, application, info, hostName);
        this.registar = new WorkerRegistar(this.zooKeeper.getZooKeeper());
        this.agentRegistar = new AgentRegistar(this.zooKeeper.getZooKeeper());
        this.consumer = new WorkerConsumer(this.hazelcast, this.id);
        this.dispatcher = new PoolDispatcher(this.hazelcast);
        this.agentKeyLookup = new AgentKeyClientLookup(this.hazelcast);
    }

    public WorkerConsumer getConsumer()
    {
        return this.consumer;
    }

    public PoolDispatcher getDispatcher()
    {
        return this.dispatcher;
    }
    
    public AgentKeyClientLookup getAgentKeyLookup()
    {
        return this.agentKeyLookup;
    }
    
    public void register(Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws KeeperException, InterruptedException
    {     
        this.registar.registerWorker(new WorkerRegistration(this.id, System.currentTimeMillis(), false, this.application, this.info, this.hostName, restrictedSiteIds, workerPool, availableEngines));
    }
    
    public void unregister() throws KeeperException, InterruptedException
    {
        this.registar.unregisterWorker(this.id);
    }

    public void registerAgent(UUID agentId) throws KeeperException, InterruptedException
    {
        this.agentRegistar.registerAgent(new AgentRegistration(agentId, System.currentTimeMillis(), this.id));
    }
    
    public void unregisterAgent(UUID agentId) throws KeeperException, InterruptedException
    {
        this.agentRegistar.unregisterAgent(agentId);
    }
}
