package com.intrbiz.bergamot.cluster.client.hz;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZNotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZWorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.hz.HZProcessorDispatcher;
import com.intrbiz.bergamot.cluster.lookup.AgentKeyLookup;
import com.intrbiz.bergamot.cluster.lookup.ProxyKeyLookup;
import com.intrbiz.bergamot.cluster.lookup.hz.HZAgentKeyClientLookup;
import com.intrbiz.bergamot.cluster.lookup.hz.HZProxyKeyClientLookup;
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
public class HZProxyClient extends HZBergamotClient implements ProxyClient
{
    private final WorkerRegistar workerRegistar;
    
    private final ProcessorDispatcher processorDispatcher;
    
    private final AgentKeyLookup agentKeyLookup;
    
    private final ProxyKeyLookup proxyKeyLookup;
    
    private final AgentRegistar agentRegistar;
    
    private final NotifierRegistar registar;

    public HZProxyClient(ClusterCfg config, Consumer<Void> onPanic) throws Exception
    {
        super(config, onPanic);
        this.registar = new NotifierRegistar(this.zooKeeper.getZooKeeper());
        this.workerRegistar = new WorkerRegistar(this.zooKeeper.getZooKeeper());
        this.agentRegistar = new AgentRegistar(this.zooKeeper.getZooKeeper());
        this.processorDispatcher = new HZProcessorDispatcher(this.hazelcast, this.workerRegistar.getRouteTable());
        this.agentKeyLookup = new HZAgentKeyClientLookup(this.hazelcast);
        this.proxyKeyLookup = new HZProxyKeyClientLookup(this.hazelcast);
    }

    public ProcessorDispatcher getProcessorDispatcher()
    {
        return this.processorDispatcher;
    }
    
    public AgentKeyLookup getAgentKeyLookup()
    {
        return this.agentKeyLookup;
    }
    
    public ProxyKeyLookup getProxyKeyLookup()
    {
        return this.proxyKeyLookup;
    }
    
    public WorkerConsumer registerWorker(String application, String info, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws KeeperException, InterruptedException
    {
        UUID workerId = UUID.randomUUID();
        this.workerRegistar.registerWorker(new WorkerRegistration(workerId, System.currentTimeMillis(), true, this.id, application, info, this.hostName, restrictedSiteIds, workerPool, availableEngines));
        return new HZWorkerConsumer(this.hazelcast, workerId);
    }
    
    public void unregisterWorker(UUID workerId) throws KeeperException, InterruptedException
    {
        this.workerRegistar.unregisterWorker(workerId);
    }

    public void registerAgent(UUID agentId, UUID workerId) throws KeeperException, InterruptedException
    {
        this.agentRegistar.registerAgent(new AgentRegistration(agentId, System.currentTimeMillis(), workerId));
    }
    
    public void unregisterAgent(UUID agentId) throws KeeperException, InterruptedException
    {
        this.agentRegistar.unregisterAgent(agentId);
    }
    
    public NotificationConsumer registerNotifier(String application, String info, Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws KeeperException, InterruptedException
    {
        UUID notifierId = UUID.randomUUID();
        this.registar.registerNotifier(new NotifierRegistration(notifierId, System.currentTimeMillis(), true, this.id, application, info, this.hostName, restrictedSiteIds, availableEngines));
        return new HZNotificationConsumer(this.hazelcast, notifierId);
    }
    
    public void unregisterNotifier(UUID notifierId) throws KeeperException, InterruptedException
    {
        this.registar.unregisterNotifier(notifierId);
    }
}
