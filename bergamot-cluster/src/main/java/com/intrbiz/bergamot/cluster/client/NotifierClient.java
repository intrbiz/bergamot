package com.intrbiz.bergamot.cluster.client;

import java.util.Set;
import java.util.UUID;

import org.apache.zookeeper.KeeperException;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;

/**
 * A Bergamot Notifier node client
 */
public interface NotifierClient extends BergamotClient
{
    NotificationConsumer getNotifierConsumer();
    
    void registerNotifier(Set<UUID> restrictedSiteIds, Set<String> availableEngines) throws KeeperException, InterruptedException;
    
    void unregisterNotifier() throws KeeperException, InterruptedException;
}
