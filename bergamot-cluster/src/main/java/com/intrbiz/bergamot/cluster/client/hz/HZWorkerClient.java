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
import com.intrbiz.bergamot.cluster.registry.AgentRegistar;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistar;
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
    
    private final AgentRegistar agentRegistar;
    
    private final ProcessorRegistry processorRegistry;
    
    private final ProcessorDispatcher processorDispatcher;
    
    private final WorkerConsumer workerConsumer;

    public HZWorkerClient(Consumer<Void> onPanic, String application, String info, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws Exception
    {
        super(onPanic);
        this.application = application;
        this.info = info;
        this.workerRegistar = new WorkerRegistar(this.zooKeeper.getZooKeeper());
        this.agentRegistar = new AgentRegistar(this.zooKeeper.getZooKeeper());
        this.processorRegistry = new ProcessorRegistry(this.zooKeeper.getZooKeeper());
        this.processorDispatcher = new HZProcessorDispatcher(this.hazelcast, this.processorRegistry.getRouteTable());
        this.workerConsumer = new HZWorkerConsumer(this.hazelcast, this.id);
        // Register this worker
        this.registerWorker(restrictedSiteIds, workerPool, availableEngines);
    }

    @Override
    public WorkerConsumer getWorkerConsumer()
    {
        return this.workerConsumer;
    }

    @Override
    public ProcessorDispatcher getProcessorDispatcher()
    {
        return this.processorDispatcher;
    }
    
    private void registerWorker(Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws KeeperException, InterruptedException
    {     
        this.workerRegistar.registerWorker(new WorkerRegistration(this.id, System.currentTimeMillis(), this.application, this.info, this.hostName, restrictedSiteIds, workerPool, availableEngines));
    }
    
    private void unregisterWorker() throws KeeperException, InterruptedException
    {
        this.workerRegistar.unregisterWorker(this.id);
    }

    @Override
    public void registerAgent(UUID agentId, UUID nonce) throws KeeperException, InterruptedException
    {
        this.agentRegistar.registerAgent(new AgentRegistration(agentId, nonce, System.currentTimeMillis(), this.id));
    }
    
    @Override
    public void unregisterAgent(UUID agentId, UUID nonce) throws KeeperException, InterruptedException
    {
        this.agentRegistar.unregisterAgent(agentId, nonce, this.id);
    }

    @Override
    public void close()
    {
        try
        {
            this.unregisterWorker();
        }
        catch (Exception e)
        {
        }
        super.close();
    }
}
