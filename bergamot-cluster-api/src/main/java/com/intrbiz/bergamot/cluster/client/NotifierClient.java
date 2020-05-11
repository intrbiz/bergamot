package com.intrbiz.bergamot.cluster.client;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;

/**
 * A Bergamot Notifier node client
 */
public interface NotifierClient extends BergamotClient
{
    NotificationConsumer getNotifierConsumer();
}
