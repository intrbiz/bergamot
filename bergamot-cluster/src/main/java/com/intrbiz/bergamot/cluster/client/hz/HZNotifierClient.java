package com.intrbiz.bergamot.cluster.client.hz;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.client.NotifierClient;
import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;
import com.intrbiz.bergamot.cluster.consumer.hz.HZNotificationConsumer;
import com.intrbiz.bergamot.cluster.registry.NotifierRegistar;
import com.intrbiz.bergamot.model.message.cluster.NotifierRegistration;

/**
 * A Bergamot Notifier node client
 */
public class HZNotifierClient extends HZBergamotClient implements NotifierClient
{   
    protected final String application;
    
    protected final String info;
    
    private final NotifierRegistar registar;
    
    private final NotificationConsumer consumer;

    public HZNotifierClient(Consumer<Void> onPanic, String application, String info, Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws Exception
    {
        super(onPanic);
        this.application = application;
        this.info = info;
        this.registar = new NotifierRegistar(this.zooKeeper.getZooKeeper());
        this.consumer = new HZNotificationConsumer(this.hazelcast, this.id);
        // Register this notifier
        this.registerNotifier(restrictedSiteIds, availableEngines);
    }

    @Override
    public NotificationConsumer getNotifierConsumer()
    {
        return this.consumer;
    }
    
    private void registerNotifier(Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws KeeperException, InterruptedException
    {     
        this.registar.registerNotifier(new NotifierRegistration(this.id, System.currentTimeMillis(), this.application, this.info, this.hostName, restrictedSiteIds, availableEngines));
    }
    
    private void unregisterNotifier() throws KeeperException, InterruptedException
    {
        this.registar.unregisterNotifier(this.id);
    }
    
    @Override
    public void close()
    {
        try
        {
            this.unregisterNotifier();
        }
        catch (Exception e)
        {
        }
        super.close();
    }
}
