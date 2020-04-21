package com.intrbiz.bergamot.cluster.client;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyClientLookup;
import com.intrbiz.bergamot.cluster.lookup.ProxyKeyClientLookup;
import com.intrbiz.bergamot.cluster.registry.AgentRegistar;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistar;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistar;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.model.message.cluster.AgentRegistration;
import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * A Bergamot Proxy node client
 */
public class ProxyClient extends BergamotClient
{
    private final WorkerRegistar workerRegistar;
    
    private final ProcessorDispatcher processorDispatcher;
    
    private final AgentKeyClientLookup agentKeyLookup;
    
    private final ProxyKeyClientLookup proxyKeyLookup;
    
    private final AgentRegistar agentRegistar;
    
    private final NotifierRegistar registar;

    public ProxyClient(ClusterCfg config, Consumer<Void> onPanic) throws Exception
    {
        super(config, onPanic);
        this.registar = new NotifierRegistar(this.zooKeeper.getZooKeeper());
        this.workerRegistar = new WorkerRegistar(this.zooKeeper.getZooKeeper());
        this.agentRegistar = new AgentRegistar(this.zooKeeper.getZooKeeper());
        this.processorDispatcher = new ProcessorDispatcher(this.hazelcast, this.workerRegistar.getRouteTable());
        this.agentKeyLookup = new AgentKeyClientLookup(this.hazelcast);
        this.proxyKeyLookup = new ProxyKeyClientLookup(this.hazelcast);
    }

    public ProcessorDispatcher getProcessorDispatcher()
    {
        return this.processorDispatcher;
    }
    
    public AgentKeyClientLookup getAgentKeyLookup()
    {
        return this.agentKeyLookup;
    }
    
    public ProxyKeyClientLookup getProxyKeyLookup()
    {
        return this.proxyKeyLookup;
    }
    
    public WorkerConsumer registerWorker(String application, String info, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws KeeperException, InterruptedException
    {
        UUID workerId = UUID.randomUUID();
        this.workerRegistar.registerWorker(new WorkerRegistration(workerId, System.currentTimeMillis(), true, this.id, application, info, this.hostName, restrictedSiteIds, workerPool, availableEngines));
        return new WorkerConsumer(this.hazelcast, workerId);
    }
    
    public void unregisterWorker(UUID workerId) throws KeeperException, InterruptedException
    {
        this.workerRegistar.unregisterWorker(workerId);
    }

    public void registerAgent(UUID agentId) throws KeeperException, InterruptedException
    {
        this.agentRegistar.registerAgent(new AgentRegistration(agentId, System.currentTimeMillis(), this.id));
    }
    
    public void unregisterAgent(UUID agentId) throws KeeperException, InterruptedException
    {
        this.agentRegistar.unregisterAgent(agentId);
    }
    
    public NotificationConsumer registerNotifier(String application, String info, Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws KeeperException, InterruptedException
    {
        UUID notifierId = UUID.randomUUID();
        this.registar.registerNotifier(new NotifierRegistration(notifierId, System.currentTimeMillis(), true, this.id, application, info, this.hostName, restrictedSiteIds, availableEngines));
        return new NotificationConsumer(this.hazelcast, notifierId);
    }
    
    public void unregisterNotifier(UUID notifierId) throws KeeperException, InterruptedException
    {
        this.registar.unregisterNotifier(notifierId);
    }
}
