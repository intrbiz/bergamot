package com.intrbiz.bergamot.cluster.dispatcher;

import com.intrbiz.bergamot.cluster.model.PublishStatus;
import com.intrbiz.bergamot.model.message.notification.Notification;

/**
 * Dispatch notifications to notifiers
 */
public interface NotificationDispatcher
{    
    PublishStatus dispatchNotification(Notification notification);
}
