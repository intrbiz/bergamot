package com.intrbiz.bergamot.worker;

import com.intrbiz.bergamot.model.message.notification.Notification;

public interface NotificationExecutor<T extends Engine> extends Executor<T>
{
    /**
     * Execute the notification
     */
    void run(Notification notification);
}
