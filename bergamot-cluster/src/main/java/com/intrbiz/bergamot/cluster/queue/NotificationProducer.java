package com.intrbiz.bergamot.cluster.queue;

import com.intrbiz.bergamot.model.message.notification.Notification;

public interface NotificationProducer
{
    /**
     * Send a notification
     * @param notification the notification to send
     */
    void sendNotification(Notification notification);
}
