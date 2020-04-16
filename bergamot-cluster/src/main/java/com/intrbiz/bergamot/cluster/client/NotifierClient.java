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
 * A Bergamot Worker node client
 */
public class NotifierClient extends BergamotClient
{    
    private final NotifierRegistar registar;
    
    private final NotificationConsumer consumer;

    public NotifierClient(ClusterCfg config, Consumer<Void> onPanic, String application, String info) throws Exception
    {
        super(config, onPanic, application, info);
        this.registar = new NotifierRegistar(this.zooKeeper.getZooKeeper());
        this.consumer = new NotificationConsumer(this.hazelcast, this.id);
    }

    public NotificationConsumer getConsumer()
    {
        return this.consumer;
    }
    
    public void register(Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws KeeperException, InterruptedException
    {     
        this.registar.registerNotifier(new NotifierRegistration(this.id, System.currentTimeMillis(), false, this.application, this.info, this.hostName, restrictedSiteIds, availableEngines));
    }
    
    public void unregister() throws KeeperException, InterruptedException
    {
        this.registar.unregisterNotifier(this.id);
    }
}
