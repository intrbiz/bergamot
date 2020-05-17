package com.intrbiz.bergamot.cluster.client.hz;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.client.ProxyClient;
import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.ProxyConsumer;
import com.intrbiz.bergamot.cluster.consumer.WorkerConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZNotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZProxyConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZWorkerConsumer;
import com.intrbiz.bergamot.cluster.dispatcher.ProcessorDispatcher;
import com.intrbiz.bergamot.cluster.dispatcher.hz.HZProcessorDispatcher;
import com.intrbiz.bergamot.cluster.registry.AgentRegistar;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistar;
import com.intrbiz.bergamot.cluster.registry.ProcessorRegistry;
import com.intrbiz.bergamot.cluster.registry.ProxyRegistar;
import com.intrbiz.bergamot.cluster.registry.WorkerRegistar;
import com.intrbiz.bergamot.model.message.cluster.AgentRegistration;
import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;
import com.intrbiz.bergamot.model.message.cluster.ProxyRegistration;
import com.intrbiz.bergamot.model.message.cluster.WorkerRegistration;

/**
 * A Bergamot Proxy node client
 */
public class HZProxyClient extends HZBergamotClient implements ProxyClient
{
    protected final String application;
    
    protected final String info;
    
    private final ProxyRegistar proxyRegistar;
    
    private final WorkerRegistar workerRegistar;
    
    private final AgentRegistar agentRegistar;
    
    private final NotifierRegistar notifierRegistar;
    
    private final ProcessorRegistry processorRegistry;
    
    private final ProcessorDispatcher processorDispatcher;
    
    private final ProxyConsumer proxyConsumer;

    public HZProxyClient(Consumer<Void> onPanic, String application, String info) throws Exception
    {
        super(onPanic);
        this.application = application;
        this.info = info;
        this.proxyRegistar = new ProxyRegistar(this.zooKeeper.getZooKeeper());
        this.notifierRegistar = new NotifierRegistar(this.zooKeeper.getZooKeeper());
        this.workerRegistar = new WorkerRegistar(this.zooKeeper.getZooKeeper());
        this.agentRegistar = new AgentRegistar(this.zooKeeper.getZooKeeper());
        this.processorRegistry = new ProcessorRegistry(this.zooKeeper.getZooKeeper());
        this.processorDispatcher = new HZProcessorDispatcher(this.hazelcast, this.processorRegistry.getRouteTable());
        this.proxyConsumer = new HZProxyConsumer(this.hazelcast, this.id);
        // register this proxy
        this.registerProxy();
    }

    @Override
    public ProxyConsumer getProxyConsumer()
    {
        return this.proxyConsumer;
    }

    @Override
    public ProcessorDispatcher getProcessorDispatcher()
    {
        return this.processorDispatcher;
    }
    
    private void registerProxy() throws KeeperException, InterruptedException
    {     
        this.proxyRegistar.registerProxy(new ProxyRegistration(this.id, System.currentTimeMillis(), this.application, this.info, this.hostName));
    }
    
    private void unregisterProxy() throws KeeperException, InterruptedException
    {
        this.workerRegistar.unregisterWorker(this.id);
    }
    
    @Override
    public WorkerConsumer registerWorker(String host, String application, String info, Set<UUID> restrictedSiteIds, String workerPool, Set<String> availableEngines) throws KeeperException, InterruptedException
    {
        UUID workerId = UUID.randomUUID();
        this.workerRegistar.registerWorker(new WorkerRegistration(workerId, System.currentTimeMillis(), true, this.id, application, info, host, restrictedSiteIds, workerPool, availableEngines));
        return new HZWorkerConsumer(this.hazelcast, workerId);
    }
    
    @Override
    public void unregisterWorker(UUID workerId) throws KeeperException, InterruptedException
    {
        this.workerRegistar.unregisterWorker(workerId);
    }

    @Override
    public void registerAgent(UUID agentId, UUID workerId) throws KeeperException, InterruptedException
    {
        this.agentRegistar.registerAgent(new AgentRegistration(agentId, System.currentTimeMillis(), workerId));
    }
    
    @Override
    public void unregisterAgent(UUID agentId, UUID workerId) throws KeeperException, InterruptedException
    {
        this.agentRegistar.unregisterAgent(agentId, workerId);
    }

    @Override
    public NotificationConsumer registerNotifier(String host, String application, String info, Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws KeeperException, InterruptedException
    {
        UUID notifierId = UUID.randomUUID();
        this.notifierRegistar.registerNotifier(new NotifierRegistration(notifierId, System.currentTimeMillis(), true, this.id, application, info, host, restrictedSiteIds, availableEngines));
        return new HZNotificationConsumer(this.hazelcast, notifierId);
    }
    
    @Override
    public void unregisterNotifier(UUID notifierId) throws KeeperException, InterruptedException
    {
        this.notifierRegistar.unregisterNotifier(notifierId);
    }
    
    @Override
    public void close()
    {
        try
        {
            this.unregisterProxy();
        }
        catch (Exception e)
        {
        }
        super.close();
    }
}
