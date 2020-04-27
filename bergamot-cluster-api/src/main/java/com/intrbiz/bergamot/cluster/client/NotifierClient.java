package com.intrbiz.bergamot.cluster.client;

import java.util.Set;
import java.util.UUID;

import com.intrbiz.bergamot.cluster.consumer.NotificationConsumer;

/**
 * A Bergamot Notifier node client
 */
public interface NotifierClient extends BergamotClient
{
    NotificationConsumer getNotifierConsumer();
}
