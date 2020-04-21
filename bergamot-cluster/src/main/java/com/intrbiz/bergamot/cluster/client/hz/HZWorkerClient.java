package com.intrbiz.bergamot.cluster.client.hz;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.client.WorkerClient;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZWorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.hz.HZProcessorDispatcher;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.cluster.lookup.hz.HZAgentKeyClientLookup;
import com.intrbiz.bergamot.cluster.registry.AgentRegistar;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistar;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.model.message.cluster.AgentRegistration;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * A Bergamot Worker node client
 */
public class HZWorkerClient extends HZBergamotClient implements WorkerClient
{   
    protected final String application;
    
    protected final String info;
    
    private final WorkerRegistar workerRegistar;
    
    private final WorkerConsumer workerConsumer;
    
    private final ProcessorDispatcher processorDispatcher;
    
    private final AgentKeyLookup agentKeyLookup;
    
    private final AgentRegistar agentRegistar;

    public HZWorkerClient(ClusterCfg config, Consumer<Void> onPanic, String application, String info) throws Exception
    {
        super(config, onPanic);
        this.application = application;
        this.info = info;
        this.workerRegistar = new WorkerRegistar(this.zooKeeper.getZooKeeper());
        this.agentRegistar = new AgentRegistar(this.zooKeeper.getZooKeeper());
        this.workerConsumer = new HZWorkerConsumer(this.hazelcast, this.id);
        this.processorDispatcher = new HZProcessorDispatcher(this.hazelcast, this.workerRegistar.getRouteTable());
        this.agentKeyLookup = new HZAgentKeyClientLookup(this.hazelcast);
    }

    public WorkerConsumer getWorkerConsumer()
    {
        return this.workerConsumer;
    }

    public ProcessorDispatcher getProcessorDispatcher()
    {
        return this.processorDispatcher;
    }
    
    public AgentKeyLookup getAgentKeyLookup()
    {
        return this.agentKeyLookup;
    }
    
    public void registerWorker(Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws KeeperException, InterruptedException
    {     
        this.workerRegistar.registerWorker(new WorkerRegistration(this.id, System.currentTimeMillis(), this.application, this.info, this.hostName, restrictedSiteIds, workerPool, availableEngines));
    }
    
    public void unregisterWorker() throws KeeperException, InterruptedException
    {
        this.workerRegistar.unregisterWorker(this.id);
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
