package com.intrbiz.bergamot.cluster.client;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistar;
import com.intrbiz.bergamot.config.ClusterCfg;
import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;

/**
 * A Bergamot Notifier node client
 */
public class NotifierClient extends BergamotClient
{   
    protected final String application;
    
    protected final String info;
    
    private final NotifierRegistar registar;
    
    private final NotificationConsumer consumer;

    public NotifierClient(ClusterCfg config, Consumer<Void> onPanic, String application, String info) throws Exception
    {
        super(config, onPanic);
        this.application = application;
        this.info = info;
        this.registar = new NotifierRegistar(this.zooKeeper.getZooKeeper());
        this.consumer = new NotificationConsumer(this.hazelcast, this.id);
    }

    public NotificationConsumer getNotifierConsumer()
    {
        return this.consumer;
    }
    
    public void registerNotifier(Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws KeeperException, InterruptedException
    {     
        this.registar.registerNotifier(new NotifierRegistration(this.id, System.currentTimeMillis(), this.application, this.info, this.hostName, restrictedSiteIds, availableEngines));
    }
    
    public void unregisterNotifier() throws KeeperException, InterruptedException
    {
        this.registar.unregisterNotifier(this.id);
    }
}
